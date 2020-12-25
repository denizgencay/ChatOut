package arsi.dev.chatout;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import arsi.dev.chatout.adapters.MessageRecyclerAdapter;
import arsi.dev.chatout.cards.MessageCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private ArrayList<String> colors;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private EditText messageText;
    private ArrayList<MessageCard> messageCards;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    private String chatId, creatorUid, senderUsername, usernameColor, titleText;
    private ArrayList<String> peopleInChat, blockedArray, errorGivenUids;
    private String uid;
    private boolean isFirstReload, inChat, firstMessage;
    private ImageButton send;
    private RecyclerView recyclerView;
    private ImageView exit, people;
    private ListenerRegistration task, task1;
    private TextView title;
    private ArrayList<Boolean> colorCtr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        uid = firebaseAuth.getCurrentUser().getUid();

        messageText = findViewById(R.id.message_text);
        send = findViewById(R.id.message_send_message);
        exit = findViewById(R.id.message_exit);
        title = findViewById(R.id.message_title);
        people = findViewById(R.id.message_people);

        peopleInChat = new ArrayList<>();
        blockedArray = new ArrayList<>();
        messageCards = new ArrayList<>();
        errorGivenUids = new ArrayList<>();
        firstMessage = true;
        isFirstReload = true;
        usernameColor = "#000000";

        initializeColorCtrs();
        initializeColors();

        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");

        getMessages();

        messageRecyclerAdapter = new MessageRecyclerAdapter(messageCards, this);
        recyclerView = findViewById(R.id.messageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageRecyclerAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();
                Timestamp time = new Timestamp(date);
                String message;
                message = messageText.getText().toString().trim();

                if (!message.isEmpty()) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("sender", uid);
                    data.put("message", message);
                    data.put("time", time);
                    data.put("senderUsername", senderUsername);
                    if (firstMessage) {
                        int index = colorCtr.indexOf(false);
                        if (index != -1) data.put("usernameColor", colors.get(index));
                        else data.put("usernameColor", colors.get(new Random().nextInt(5)));
                    } else data.put("usernameColor", usernameColor);

                    CollectionReference collectionReference = firebaseFirestore.collection("chats").document(chatId).collection("messages");
                    collectionReference.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MessageActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    for (String userUid : peopleInChat) {
                        if (!userUid.equals(uid)) {
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(userUid);
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String pushToken = documentSnapshot.getString("pushToken");
                                    if (pushToken != null)
                                        sendNotification(pushToken, message, senderUsername);
                                }
                            });
                        }
                    }

                    messageText.setText("");
                }
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit.setClickable(false);
                if (uid.equals(creatorUid) && peopleInChat.size() > 1) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MessageActivity.this);
                    alert.setTitle("Dikkat");
                    alert.setMessage("Konuşmanın moderatörü olduğunuz için ayrılmadan önce sizden yeni bir moderatör seçmenizi istiyoruz.");
                    alert.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exit.setClickable(true);
                            Intent intent = new Intent(MessageActivity.this, ChangeModeratorActivity.class);
                            intent.putExtra("chatId", chatId);
                            intent.putExtra("insideChat", true);
                            startActivity(intent);
                        }
                    });
                    alert.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exit.setClickable(true);
                        }
                    });
                    alert.show();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MessageActivity.this);
                    alert.setTitle("Dikkat");
                    alert.setMessage("Konuşmadan ayrılıyorsun yerini başkası alabilir");
                    alert.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.chatChanging = true;
                            String activeChat = "";
                            Map<String, Object> data = new HashMap<>();
                            data.put("activeChat", activeChat);
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                            documentReference.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    HashMap<String, Object> update = new HashMap<>();
                                    update.put("peopleInChat", FieldValue.arrayRemove(uid));
                                    DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(chatId);
                                    documentReference1.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent intent1 = new Intent(MessageActivity.this, MainPage.class);
                                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent1);
                                        }
                                    });
                                }
                            });
                        }
                    });
                    alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exit.setClickable(true);
                        }
                    });
                    alert.show();
                }
            }
        });

        people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageActivity.this, PeopleInChatActivity.class);
                intent.putExtra("chatId", chatId);
                startActivity(intent);
            }
        });
    }

    private void getDataFromFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        task1 = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(MessageActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    senderUsername = (String) data.get("username");
                    blockedArray = (ArrayList<String>) data.get("blockedArray");

                    DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(chatId);
                    task = documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Toast.makeText(MessageActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                            if (documentSnapshot != null) {

                                Map<String, Object> data1 = documentSnapshot.getData();
                                peopleInChat = (ArrayList<String>) data1.get("peopleInChat");
                                creatorUid = (String) data1.get("creatorUid");

                                titleText = (String) data1.get("title");
                                title.setText(titleText);

                                for (String userUid : peopleInChat) {
                                    if (blockedArray.contains(userUid) && !errorGivenUids.contains(userUid)) {
                                        errorGivenUids.add(userUid);
                                        if (!isFirstReload) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                                            builder.setTitle("Engellediğin kişi konuşmaya girdi");
                                            builder.setMessage("İstersen çıkabilirsin");
                                            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    App.chatChanging = true;
                                                    HashMap<String, Object> update = new HashMap<>();
                                                    update.put("activeChat", "");
                                                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            HashMap<String, Object> chatUpdate = new HashMap<>();
                                                            chatUpdate.put("peopleInChat", FieldValue.arrayRemove(uid));
                                                            documentReference1.set(chatUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Intent intent = new Intent(MessageActivity.this, MainPage.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                            builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            if (inChat) builder.show();
                                        }
                                    }
                                }
                                for (String userUid : errorGivenUids) {
                                    if (!peopleInChat.contains(userUid))
                                        errorGivenUids.remove(userUid);
                                }

                                if (inChat) {
                                    if (!peopleInChat.contains(uid)) {
                                        Intent intent = new Intent(MessageActivity.this, MainPage.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                                isFirstReload = false;
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        inChat = false;
        task.remove();
        task1.remove();
        MyFirebaseMessaging.inChat = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        inChat = true;
        isFirstReload = true;
        MyFirebaseMessaging.inChat = true;
        App.notificationBodies.clear();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifs = notificationManager.getActiveNotifications();
        if (notifs != null) if (notifs.length > 0) notificationManager.cancel(0);
        getDataFromFirestore();
    }

    private void getMessages() {
        CollectionReference collectionReference = firebaseFirestore.collection("chats").document(chatId).collection("messages");
        collectionReference.orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                messageCards.clear();
                if (e != null) {
                    Toast.makeText(MessageActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {

                        Map<String, Object> data = snapshot.getData();
                        String message = (String) data.get("message");
                        String senderUsername = (String) data.get("senderUsername");
                        String sender = (String) data.get("sender");
                        String cardUsernameColor = (String) data.get("usernameColor");
                        colorCtr.set(colors.indexOf(cardUsernameColor), true);
                        Timestamp time = (Timestamp) data.get("time");

                        if (sender.equals(firebaseAuth.getCurrentUser().getUid())) {
                            firstMessage = false;
                            usernameColor = (String) data.get("usernameColor");
                        }

                        MessageCard card = new MessageCard(message, senderUsername, time, sender, cardUsernameColor);
                        messageCards.add(card);
                        messageRecyclerAdapter.notifyDataSetChanged();
                    }
                    recyclerView.scrollToPosition(messageRecyclerAdapter.getItemCount() - 1);
                }
            }
        });
    }

    private void sendNotification(String pushToken, String message, String senderUsername) {
        JsonObject payload = buildNotificationPayload(pushToken, message, senderUsername);
        ApiClient.getApiService().sendNotification(payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MessageActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JsonObject buildNotificationPayload(String pushToken, String message, String senderUsername) {
        JsonObject payload = new JsonObject();

        payload.addProperty("to", pushToken);

        String body = senderUsername + ": " + message;

        JsonObject data = new JsonObject();
        data.addProperty("key", "newMessage");
        data.addProperty("chatId", chatId);
        data.addProperty("title", titleText);
        data.addProperty("body", body);

        payload.add("data", data);

        return payload;
    }

    private void initializeColors() {
        this.colors = new ArrayList<>();
        colors.add("#A93226");
        colors.add("#EC7063");
        colors.add("#21618C");
        colors.add("#1E8449");
        colors.add("#D68910");
        colors.add("#5D6D7E");
    }

    private void initializeColorCtrs() {
        this.colorCtr = new ArrayList<>();
        colorCtr.add(false);
        colorCtr.add(false);
        colorCtr.add(false);
        colorCtr.add(false);
        colorCtr.add(false);
        colorCtr.add(false);
    }
}
