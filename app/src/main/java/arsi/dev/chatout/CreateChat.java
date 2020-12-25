package arsi.dev.chatout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import arsi.dev.chatout.models.CreateChatModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CreateChat extends AppCompatActivity {

    private Spinner timeSelect, numberSelect;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private String time, number, myUsername;
    private EditText titleText;
    private CreateChatModel createChatModel;
    private ArrayList<String> notificationOpenedByUids;
    private boolean isButton12, isButton24, isPremium, isCreator;
    private Button button12, button24, share;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        createChatModel = new CreateChatModel();

        titleText = findViewById(R.id.titleView);
        numberSelect = findViewById(R.id.numberSpinner);
        button12 = findViewById(R.id.button_12);
        button24 = findViewById(R.id.button_24);
        share = findViewById(R.id.create_chat_share);
        time = "";

        notificationOpenedByUids = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        interstitialAd = new InterstitialAd(CreateChat.this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        isButton12 = false;
        isButton24 = false;

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share.setClickable(false);
                String title = titleText.getText().toString().trim();
                String uid = firebaseAuth.getCurrentUser().getUid();

                if (title.isEmpty() || time.isEmpty() || (number.isEmpty() || number.equals("İstenen Kişi Sayısı"))) {
                    Toast.makeText(CreateChat.this, "Lütfen bütün alanları doldurunuz.", Toast.LENGTH_SHORT).show();
                    share.setClickable(true);
                } else {
                    if (title.length() < 5) {
                        share.setClickable(true);
                        Toast.makeText(CreateChat.this, "Başlık 5 karakterden kısa olamaz.", Toast.LENGTH_SHORT).show();
                    } else {
                        Date date = new Date();
                        long milliSeconds = 0;
                        switch (time) {
                            case "12":
                                milliSeconds = 1000 * 60 * 60 * 12;
                                break;
                            case "24":
                                milliSeconds = 1000 * 60 * 60 * 12 * 2;
                                break;
                        }

                        long pushTime = date.getTime() + milliSeconds;
                        Date pushDate = new Date(pushTime);
                        Timestamp timestamp = new Timestamp(pushDate);

                        ArrayList<String> peopleInChat = new ArrayList<>();
                        peopleInChat.add(uid);

                        HashMap<String, Object> data = new HashMap<>();
                        data.put("peopleInChat", peopleInChat);
                        data.put("numberOfPerson", number);
                        data.put("creatorUid", uid);
                        data.put("finishTime", timestamp);
                        data.put("title", title);
                        data.put("blockedUsers", new ArrayList<>());

                        CollectionReference collectionReference = firebaseFirestore.collection("chats");
                        collectionReference.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(final DocumentReference documentReference) {
                                final String chatId = documentReference.getId();
                                final DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
                                documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String oldChatUid = (String) documentSnapshot.get("activeChat");
                                        if (!oldChatUid.isEmpty()) {
                                            if (isCreator) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateChat.this);
                                                builder.setTitle("Dikkat");
                                                builder.setMessage("Bulunduğunuz konuşmada moderatörsünüz, başka bir konuşmaya girmeden önce lütfen yeni bir moderatör belirleyin.");
                                                builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        HashMap<String, Object> sendHashmap = new HashMap<>();
                                                        sendHashmap.put("key", createChatModel.getPushTokens());

                                                        Intent intent = new Intent(CreateChat.this, ChangeModeratorActivity.class);
                                                        intent.putExtra("chatId", oldChatUid);
                                                        intent.putExtra("newChatId", chatId);
                                                        intent.putExtra("createChat", true);
                                                        intent.putExtra("keys", sendHashmap);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                                builder.show();
                                            } else {
                                                App.chatChanging = true;
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("activeChat", chatId);
                                                documentReference1.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        HashMap<String, Object> update = new HashMap<>();
                                                        update.put("peopleInChat", FieldValue.arrayRemove(uid));
                                                        DocumentReference documentReference3 = firebaseFirestore.collection("chats").document(oldChatUid);
                                                        documentReference3.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                ArrayList<String> keys = new ArrayList<>(createChatModel.getPushTokens().keySet());

                                                                for (String key : keys) {
                                                                    sendNotification(createChatModel.getPushTokens().get(key).toString());
                                                                }

                                                                if (!isPremium) {
                                                                    if (interstitialAd.isLoaded()) {
                                                                        interstitialAd.show();
                                                                    } else {
                                                                        Toast.makeText(CreateChat.this, "Ad not loaded", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    interstitialAd.setAdListener(new AdListener() {
                                                                        @Override
                                                                        public void onAdClosed() {
                                                                            super.onAdClosed();
                                                                            Intent intent = new Intent(CreateChat.this, MessageActivity.class);
                                                                            intent.putExtra("chatId", chatId);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    });
                                                                } else {
                                                                    Intent intent = new Intent(CreateChat.this, MessageActivity.class);
                                                                    intent.putExtra("chatId", chatId);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        } else {
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("activeChat", chatId);
                                            documentReference1.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    ArrayList<String> keys = new ArrayList<>(createChatModel.getPushTokens().keySet());

                                                    for (String key : keys) {
                                                        System.out.println(createChatModel.getPushTokens().get(key).toString());
                                                        sendNotification(createChatModel.getPushTokens().get(key).toString());
                                                    }

                                                    if (!isPremium) {
                                                        if (interstitialAd.isLoaded()) {
                                                            interstitialAd.show();
                                                        } else {
                                                            Toast.makeText(CreateChat.this, "Ad not loaded", Toast.LENGTH_SHORT).show();
                                                        }
                                                        interstitialAd.setAdListener(new AdListener() {
                                                            @Override
                                                            public void onAdClosed() {
                                                                super.onAdClosed();
                                                                Intent intent = new Intent(CreateChat.this, MessageActivity.class);
                                                                intent.putExtra("chatId", chatId);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                                    } else {
                                                        Intent intent = new Intent(CreateChat.this, MessageActivity.class);
                                                        intent.putExtra("chatId", chatId);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        share.setClickable(true);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                share.setClickable(true);
                            }
                        });
                    }

                }
            }
        });

        final Spinner spinner1 = numberSelect;
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(CreateChat.this, R.array.number_of_person, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                number = numberSelect.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(CreateChat.this, "Lütfen gir la ", Toast.LENGTH_LONG).show();

            }
        });

        getDataFromFireStore();

    }

    public void getDataFromFireStore() {
        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(CreateChat.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    String activeChat = (String) data.get("activeChat");
                    notificationOpenedByUids = (ArrayList<String>) data.get("notificationOpenedByUids");
                    isPremium = (Boolean) data.get("isPremium");
                    myUsername = (String) data.get("username");

                    if (!activeChat.isEmpty()) {
                        DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(activeChat);
                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String creatorUid = documentSnapshot.getString("creatorUid");
                                ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");

                                if (creatorUid.equals(uid) && peopleInChat.size() > 1)
                                    isCreator = true;
                            }
                        });
                    }

                    for (String followerUid : notificationOpenedByUids) {
                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(followerUid);
                        documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                String pushToken = documentSnapshot.getString("pushToken");
                                String userId = documentSnapshot.getString("userId");
                                createChatModel.getPushTokens().put(userId, pushToken);
                            }
                        });
                    }
                }
            }
        });
    }

    public void button12(View view) {
        if (!isButton12) {
            button12.setBackgroundResource(R.drawable.lj);
            button24.setBackgroundResource(R.drawable.kj);
            isButton12 = true;
            isButton24 = false;
            time = "12";
        } else {
            button12.setBackgroundResource(R.drawable.kj);
            isButton12 = false;
            time = "";
        }

    }

    public void button24(View view) {
        if (!isButton24) {
            button24.setBackgroundResource(R.drawable.lj);
            button12.setBackgroundResource(R.drawable.kj);
            isButton24 = true;
            isButton12 = false;
            time = "24";
        } else {
            button24.setBackgroundResource(R.drawable.kj);
            isButton24 = false;
            time = "";
        }
    }

    private void sendNotification(String pushToken) {
        JsonObject payload = buildNotificationPayload(pushToken);
        ApiClient.getApiService().sendNotification(payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(CreateChat.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JsonObject buildNotificationPayload(String pushToken) {
        JsonObject payload = new JsonObject();

        payload.addProperty("to", pushToken);

        String message = myUsername + " yeni bir konuşma açtı.";

        JsonObject data = new JsonObject();
        data.addProperty("key", "createChat");
        data.addProperty("userId",firebaseAuth.getCurrentUser().getUid());
        data.addProperty("title", "Yeni konuşma");
        data.addProperty("body", message);

        payload.add("data", data);

        return payload;
    }
}
