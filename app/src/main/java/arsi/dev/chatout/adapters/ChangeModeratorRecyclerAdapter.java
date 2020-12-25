package arsi.dev.chatout.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.ApiClient;
import arsi.dev.chatout.App;
import arsi.dev.chatout.ChangeModeratorActivity;
import arsi.dev.chatout.MainPage;
import arsi.dev.chatout.MessageActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.ChangeModeratorCard;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeModeratorRecyclerAdapter extends RecyclerView.Adapter<ChangeModeratorRecyclerAdapter.Holder> {

    private ArrayList<ChangeModeratorCard> people;
    private Context context;
    private ViewGroup parent;
    private ArrayList<String> blockedUsers;
    private HashMap<String,Object> keys;
    private String chatId,newChatId,myUsername;
    private ChangeModeratorActivity changeModeratorActivity;
    private boolean insideChat,createChat,isPremium;
    private InterstitialAd interstitialAd;

    public ChangeModeratorRecyclerAdapter(ArrayList<ChangeModeratorCard> people, Context context, String chatId, ChangeModeratorActivity changeModeratorActivity, boolean insideChat, String newChatId, boolean createChat, HashMap<String,Object> keys) {
        this.people = people;
        this.context = context;
        this.chatId = chatId;
        this.changeModeratorActivity = changeModeratorActivity;
        this.insideChat = insideChat;
        this.newChatId = newChatId;
        this.keys = keys;
        this.createChat = createChat;
        this.blockedUsers = new ArrayList<>();
        if (createChat) {
            interstitialAd = new InterstitialAd(context);
            interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_change_moderator,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChangeModeratorCard card = people.get(position);
        holder.username.setText(card.getUsername());
        Picasso.get().load(card.getPhotoUri()).noFade().into(holder.profilePhoto);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.card.setClickable(false);
                if (!blockedUsers.contains(card.getUserId())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Dikkat");
                    String message = card.getUsername() + " isimli kullanıcıya moderatörlüğü vermek istiyor musunuz?";
                    builder.setMessage(message);
                    builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            holder.card.setClickable(true);
                        }
                    });
                    builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (insideChat) {
                                App.chatChanging = true;
                                HashMap<String,Object> userUpdate = new HashMap<>();
                                userUpdate.put("activeChat","");
                                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                documentReference.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String,Object> update = new HashMap<>();
                                        update.put("creatorUid",card.getUserId());
                                        update.put("peopleInChat", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                                        DocumentReference documentReference1 = FirebaseFirestore.getInstance().collection("chats").document(chatId);
                                        documentReference1.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Moderatörü başarıyla değiştirip konuşmadan ayrıldınız", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(context, MainPage.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                parent.getContext().startActivity(intent);
                                                changeModeratorActivity.finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                System.out.println(e.getLocalizedMessage());
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getLocalizedMessage());
                                    }
                                });
                            } else if (createChat) {
                                if (newChatId != null) {
                                    App.chatChanging = true;
                                    HashMap<String,Object> userUpdate = new HashMap<>();
                                    userUpdate.put("activeChat",newChatId);
                                    DocumentReference documentReference1 = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    documentReference1.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            HashMap<String,Object> update = new HashMap<>();
                                            update.put("creatorUid",card.getUserId());
                                            update.put("peopleInChat", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                                            DocumentReference documentReference2 = FirebaseFirestore.getInstance().collection("chats").document(chatId);
                                            documentReference2.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    HashMap<String,Object> notificationKeys = (HashMap<String, Object>) keys.get("key");
                                                    ArrayList<String> keyList = new ArrayList<>(notificationKeys.keySet());

                                                    for(String key : keyList){
                                                        sendNotification(notificationKeys.get(key).toString());
                                                    }

                                                    if (!isPremium) {
                                                        if (interstitialAd.isLoaded()) {
                                                            interstitialAd.show();
                                                        } else {
                                                            Toast.makeText(context, "Ad not loaded", Toast.LENGTH_SHORT).show();
                                                        }
                                                        interstitialAd.setAdListener(new AdListener() {
                                                            @Override
                                                            public void onAdClosed() {
                                                                super.onAdClosed();
                                                                Intent intent = new Intent(context, MessageActivity.class);
                                                                intent.putExtra("chatId",newChatId);
                                                                parent.getContext().startActivity(intent);
                                                                changeModeratorActivity.finish();
                                                            }
                                                        });
                                                    } else {
                                                        Intent intent = new Intent(context, MessageActivity.class);
                                                        intent.putExtra("chatId",newChatId);
                                                        parent.getContext().startActivity(intent);
                                                        changeModeratorActivity.finish();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            } else {
                                if (newChatId != null) {
                                    App.chatChanging = true;
                                    HashMap<String,Object> newChatUpdate = new HashMap<>();
                                    newChatUpdate.put("peopleInChat",FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                                    DocumentReference documentReference = FirebaseFirestore.getInstance().collection("chats").document(newChatId);
                                    documentReference.update(newChatUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            HashMap<String,Object> userUpdate = new HashMap<>();
                                            userUpdate.put("activeChat",newChatId);
                                            DocumentReference documentReference1 = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            documentReference1.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    HashMap<String,Object> update = new HashMap<>();
                                                    update.put("creatorUid",card.getUserId());
                                                    update.put("peopleInChat", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                                                    DocumentReference documentReference2 = FirebaseFirestore.getInstance().collection("chats").document(chatId);
                                                    documentReference2.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Intent intent = new Intent(context, MessageActivity.class);
                                                            intent.putExtra("chatId",newChatId);
                                                            parent.getContext().startActivity(intent);
                                                            changeModeratorActivity.finish();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                        }
                    });
                    builder.show();
                } else {
                    holder.card.setClickable(true);
                    Toast.makeText(context, "Engellediğiniz bir kullanıcıya moderatörlük veremezsiniz.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        CircleImageView profilePhoto;
        TextView username;

        public Holder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.change_moderator_card);
            profilePhoto = itemView.findViewById(R.id.change_moderator_card_profile_photo);
            username = itemView.findViewById(R.id.change_moderator_card_username);
        }
    }

    public void setBlockedUsers(ArrayList<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
    }

    private void sendNotification(String pushToken) {
        JsonObject payload = buildNotificationPayload(pushToken);
        ApiClient.getApiService().sendNotification(payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JsonObject buildNotificationPayload(String pushToken) {
        JsonObject payload = new JsonObject();

        payload.addProperty("to", pushToken);

        String message = myUsername + " yeni bir konuşma açtı.";

        JsonObject data = new JsonObject();
        data.addProperty("key", "createChat");
        data.addProperty("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.addProperty("title", "Yeni konuşma");
        data.addProperty("body", message);

        payload.add("data", data);

        return payload;
    }
}
