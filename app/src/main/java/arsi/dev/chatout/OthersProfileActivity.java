package arsi.dev.chatout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import arsi.dev.chatout.models.OthersProfileModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OthersProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private TextView usernameText, aboutMe, followersText, followingText;
    private ImageView profilePicture;
    private String searchId,activeChat,myActiveChat,myUsername;
    private ArrayList<String> followers;
    private ArrayList<String> following;
    private Button goToChat,goToDict;
    private ArrayList<String> blockedArray, blockedByArray, notificationOpenedUids,myFollowings;
    private OthersProfileModel othersProfileModel;
    private ImageView blockButton,follow,notification;
    private RelativeLayout chatInfo,aboutMeView,penManView,blockedView;
    private TextView chatTitle,chatFinishTime,chatNumberOfPerson;
    private boolean isNotificationOpened,isFollowed,isCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        usernameText = findViewById(R.id.others_profile_username);
        profilePicture = findViewById(R.id.others_profile_profile_photo);
        followersText = findViewById(R.id.others_profile_followers);
        followingText = findViewById(R.id.others_profile_followings);
        notification = findViewById(R.id.others_profile_notification);
        blockButton = findViewById(R.id.others_profile_block);
        follow = findViewById(R.id.others_profile_follow);
        chatInfo = findViewById(R.id.others_profile_chat_info);
        goToChat = findViewById(R.id.others_profile_go_to_chat);
        aboutMe = findViewById(R.id.others_profile_about_me);
        chatTitle = findViewById(R.id.others_profile_chat_title);
        chatFinishTime = findViewById(R.id.others_profile_end_time);
        chatNumberOfPerson = findViewById(R.id.others_profile_attendants);
        goToDict = findViewById(R.id.others_profile_go_to_dict);
        aboutMeView = findViewById(R.id.others_profile_about_me_relative);
        penManView = findViewById(R.id.others_profile_pencil_man_relative);
        blockedView = findViewById(R.id.others_profile_blocked_view);

        isNotificationOpened = false;
        isFollowed = false;

        followers = new ArrayList<>();
        following = new ArrayList<>();
        blockedArray = new ArrayList<>();
        blockedByArray = new ArrayList<>();
        notificationOpenedUids = new ArrayList<>();
        myFollowings = new ArrayList<>();

        othersProfileModel = new OthersProfileModel();

        Intent intent = getIntent();
        searchId = intent.getStringExtra("searchIdUsername");

        getMyInfo();
        getUserProfilePhoto();
        getDataFromFireStore();

        profilePicture.setClickable(false);

        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockButton.setClickable(false);
                String uid = firebaseAuth.getCurrentUser().getUid();

                Map<String,Object> data = new HashMap<>();
                data.put("blockedArray",FieldValue.arrayUnion(searchId));
                data.put("followers",FieldValue.arrayRemove(searchId));
                data.put("followings",FieldValue.arrayRemove(searchId));
                data.put("notificationOpenedUids",FieldValue.arrayRemove(searchId));
                data.put("notificationOpenedByUids",FieldValue.arrayRemove(searchId));
                data.put("lastSearchs",FieldValue.arrayRemove(searchId));
                DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                documentReference.set(data,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> data1 = new HashMap<>();
                        data1.put("blockedByArray",FieldValue.arrayUnion(uid));
                        data1.put("followers",FieldValue.arrayRemove(uid));
                        data1.put("followings",FieldValue.arrayRemove(uid));
                        data1.put("notificationOpenedUids",FieldValue.arrayRemove(uid));
                        data1.put("notificationOpenedByUids",FieldValue.arrayRemove(uid));
                        data1.put("lastSearchs",FieldValue.arrayRemove(uid));
                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(searchId);
                        documentReference1.set(data1,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        });
                    }
                });
            }
        });

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                follow.setClickable(false);
                final String uid = firebaseAuth.getCurrentUser().getUid();
                if (!isFollowed) {
                    HashMap<String, Object> update = new HashMap<>();
                    update.put("followers", FieldValue.arrayUnion(uid));
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(searchId);
                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            HashMap<String,Object> myUpdate = new HashMap<>();
                            myUpdate.put("followings",FieldValue.arrayUnion(searchId));
                            DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
                            documentReference1.set(myUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    follow.setClickable(true);
                                    sendNotification();
                                }
                            });
                        }
                    });
                } else {
                    HashMap<String, Object> update = new HashMap<>();
                    update.put("followers", FieldValue.arrayRemove(uid));
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(searchId);
                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            HashMap<String,Object> myUpdate = new HashMap<>();
                            myUpdate.put("followings",FieldValue.arrayRemove(searchId));
                            DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
                            documentReference1.set(myUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    follow.setClickable(true);
                                }
                            });
                        }
                    });
                }
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notification.setClickable(false);
                String uid = firebaseAuth.getCurrentUser().getUid();
                if (!isNotificationOpened) {
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                    HashMap<String,Object> update = new HashMap<>();
                    update.put("notificationOpenedUids",FieldValue.arrayUnion(searchId));
                    documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            HashMap<String,Object> userUpdate = new HashMap<>();
                            userUpdate.put("notificationOpenedByUids",FieldValue.arrayUnion(uid));
                            DocumentReference documentReference1 = firebaseFirestore.collection("users").document(searchId);
                            documentReference1.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    notification.setClickable(true);
                                }
                            });
                        }
                    });
                } else {
                    HashMap<String,Object> myUpdate = new HashMap<>();
                    myUpdate.put("notificationOpenedUids",FieldValue.arrayRemove(searchId));
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                    documentReference.set(myUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            HashMap<String,Object> userUpdate = new HashMap<>();
                            userUpdate.put("notificationOpenedByUids",FieldValue.arrayRemove(uid));
                            DocumentReference documentReference1 = firebaseFirestore.collection("users").document(searchId);
                            documentReference1.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    notification.setClickable(true);
                                }
                            });
                        }
                    });
                }
            }
        });

        goToDict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OthersProfileActivity.this,EntryInfoActivity.class);
                intent.putExtra("userId",searchId);
                startActivity(intent);
            }
        });
    }

    private void getUserProfilePhoto() {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(searchId);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String photoUri = (String) documentSnapshot.getString("photoUri");
                Picasso.get().load(photoUri).noFade().into(profilePicture);
            }
        });
    }

    private void sendNotification() {
        JsonObject payload = buildNotificationPayload();
        ApiClient.getApiService().sendNotification(payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(OthersProfileActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JsonObject buildNotificationPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty("to",othersProfileModel.getPushToken());

        String message = myUsername + " sizi takip etmeye başladı.";

        JsonObject data = new JsonObject();
        data.addProperty("key","friendRequest");
        data.addProperty("userId",firebaseAuth.getCurrentUser().getUid());
        data.addProperty("title","Yeni bir takipçiniz var");
        data.addProperty("body",message);

        payload.add("data",data);

        return payload;
    }

    public void getDataFromFireStore(){
        DocumentReference documentReference = firebaseFirestore.collection("users").document(searchId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if(e != null){
                    Toast.makeText(OthersProfileActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                if(documentSnapshot != null){
                    String myUid = firebaseAuth.getCurrentUser().getUid();

                    Map<String,Object> data = documentSnapshot.getData();
                    String username = (String) data.get("username");
                    String aboutMeText = (String) data.get("aboutMe");
                    String pushToken = (String) data.get("pushToken");

                    othersProfileModel.setPushToken(pushToken);
                    followers = (ArrayList<String>) data.get("followers");
                    following = (ArrayList<String>) data.get("followings");
                    blockedByArray = (ArrayList<String>) data.get("blockedByArray");
                    activeChat = (String) data.get("activeChat");

                    if (!activeChat.isEmpty()) {
                        penManView.setVisibility(View.GONE);
                        chatInfo.setVisibility(View.VISIBLE);
                        DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(activeChat);
                        documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                String title = (String) documentSnapshot.getString("title");
                                String numberOfPerson = (String) documentSnapshot.getString("numberOfPerson");
                                ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");
                                Timestamp finishTime = (Timestamp) documentSnapshot.getTimestamp("finishTime");

                                String numberOfPersonText = peopleInChat.size() + "/" + numberOfPerson;
                                Date date = finishTime.toDate();
                                LocalTime localTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
                                String timeText = "";

                                if (localTime.getHour() < 10) {
                                    if (localTime.getMinute() < 10) {
                                        timeText = "0" + localTime.getHour() + ":0" + localTime.getMinute();
                                    } else {
                                        timeText = "0" + localTime.getHour() + ":" + localTime.getMinute();
                                    }
                                } else {
                                    if (localTime.getMinute() < 10) {
                                        timeText = localTime.getHour() + ":0" + localTime.getMinute();
                                    } else {
                                        timeText = localTime.getHour() + ":" + localTime.getMinute();
                                    }
                                }

                                boolean isContainsBlockedUser = false;

                                for (String userId : blockedArray) {
                                    if (peopleInChat.contains(userId)) {
                                        isContainsBlockedUser = true;
                                        break;
                                    }
                                }

                                if (isContainsBlockedUser) blockedView.setVisibility(View.VISIBLE);
                                else blockedView.setVisibility(View.GONE);

                                chatTitle.setText(title);
                                chatNumberOfPerson.setText(numberOfPersonText);
                                chatFinishTime.setText(timeText);
                                goToChat.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        goToChat.setClickable(false);
                                        if (peopleInChat.size() < Integer.valueOf(numberOfPerson) || peopleInChat.contains(myUid)) {
                                            if (!myActiveChat.equals(activeChat) && !myActiveChat.isEmpty()) {
                                                if (isCreator) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);
                                                    builder.setTitle("Dikkat");
                                                    builder.setMessage("Bulunduğunuz konuşmada moderatörsünüz, başka bir konuşmaya girmeden önce lütfen yeni bir moderatör belirleyin.");
                                                    builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            goToChat.setClickable(true);
                                                        }
                                                    });
                                                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            goToChat.setClickable(true);
                                                            Intent intent = new Intent(OthersProfileActivity.this,ChangeModeratorActivity.class);
                                                            intent.putExtra("chatId",myActiveChat);
                                                            intent.putExtra("newChatId",activeChat);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                    builder.show();
                                                } else {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);
                                                    builder.setTitle("Dikkat");
                                                    builder.setMessage("Eğer bu konuşmaya katılırsanız eski konuşmadaki yerinizi kaybedeceksiniz");
                                                    builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            goToChat.setClickable(true);
                                                        }
                                                    });
                                                    builder.setPositiveButton("Katıl", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            HashMap<String,Object> newUpdate = new HashMap<>();
                                                            newUpdate.put("peopleInChat",FieldValue.arrayUnion(myUid));
                                                            DocumentReference documentReference2 = firebaseFirestore.collection("chats").document(activeChat);
                                                            documentReference2.set(newUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    App.chatChanging = true;
                                                                    HashMap<String,Object> myUpdate = new HashMap<>();
                                                                    myUpdate.put("activeChat",activeChat);
                                                                    final String oldChat = myActiveChat;
                                                                    DocumentReference documentReference3 = firebaseFirestore.collection("users").document(myUid);
                                                                    documentReference3.set(myUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            HashMap<String,Object> oldUpdate = new HashMap<>();
                                                                            oldUpdate.put("peopleInChat",FieldValue.arrayRemove(myUid));
                                                                            DocumentReference documentReference4 = firebaseFirestore.collection("chats").document(oldChat);
                                                                            documentReference4.set(oldUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    goToChat.setClickable(true);
                                                                                    Intent intent = new Intent(OthersProfileActivity.this,MessageActivity.class);
                                                                                    intent.putExtra("chatId",activeChat);
                                                                                    startActivity(intent);
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            } else {
                                                if (myActiveChat.equals(activeChat)) {
                                                    goToChat.setClickable(true);
                                                    Intent intent = new Intent(OthersProfileActivity.this,MessageActivity.class);
                                                    intent.putExtra("chatId",activeChat);
                                                    startActivity(intent);
                                                } else {
                                                    HashMap<String,Object> newUpdate = new HashMap<>();
                                                    newUpdate.put("peopleInChat",FieldValue.arrayUnion(myUid));
                                                    DocumentReference documentReference2 = firebaseFirestore.collection("chats").document(activeChat);
                                                    documentReference2.update(newUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            HashMap<String,Object> myUpdate = new HashMap<>();
                                                            myUpdate.put("activeChat",activeChat);
                                                            final String oldChat = myActiveChat;
                                                            DocumentReference documentReference3 = firebaseFirestore.collection("users").document(myUid);
                                                            documentReference3.update(myUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    goToChat.setClickable(true);
                                                                    Intent intent = new Intent(OthersProfileActivity.this,MessageActivity.class);
                                                                    intent.putExtra("chatId",activeChat);
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        } else {
                                            goToChat.setClickable(true);
                                            Toast.makeText(OthersProfileActivity.this, "Bu konuşma doludur.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        chatInfo.setVisibility(View.GONE);
                    }

                    if(blockedByArray != null){
                        if(blockedByArray.contains(myUid)){
                            blockButton.setClickable(false);
                        }
                    }

                    if (!aboutMeText.isEmpty()) {
                        penManView.setVisibility(View.GONE);
                        aboutMeView.setVisibility(View.VISIBLE);
                        aboutMe.setText(aboutMeText);
                    } else {
                        aboutMeView.setVisibility(View.GONE);
                    }

                    if (aboutMeText.isEmpty() && activeChat.isEmpty()) {
                        penManView.setVisibility(View.VISIBLE);
                    }

                    usernameText.setText(username);

                    if(followers != null) {
                        followersText.setText(String.valueOf(followers.size()));
                    }

                    if(following != null) {
                        followingText.setText(String.valueOf(following.size()));
                    }
                }
            }
        });
    }

    private void getMyInfo() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
        documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(OthersProfileActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                if(documentSnapshot != null){
                    Map<String,Object> data1 = documentSnapshot.getData();
                    blockedArray = (ArrayList<String>) data1.get("blockedArray");
                    notificationOpenedUids = (ArrayList<String>) data1.get("notificationOpenedUids");
                    myFollowings = (ArrayList<String>) data1.get("followings");
                    myActiveChat = (String) data1.get("activeChat");
                    myUsername = (String) data1.get("username");

                    if (!myActiveChat.isEmpty()) {
                        DocumentReference documentReference = firebaseFirestore.collection("chats").document(myActiveChat);
                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String creatorUid = documentSnapshot.getString("creatorUid");
                                ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");

                                if (creatorUid.equals(uid) && peopleInChat.size() > 1) isCreator = true;
                            }
                        });
                    }

                    if (notificationOpenedUids.contains(searchId)) {
                        isNotificationOpened = true;
                        notification.setImageResource(R.drawable.izil);
                    } else {
                        isNotificationOpened = false;
                        notification.setImageResource(R.drawable.zil);
                    }

                    if (myFollowings.contains(searchId)) {
                        isFollowed = true;
                        follow.setImageResource(R.drawable.delete_friend);
                    } else {
                        isFollowed = false;
                        follow.setImageResource(R.drawable.ic_person_add_24dp);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyFirebaseMessaging.inOthersProfile = false;
        MyFirebaseMessaging.inOthersProfileUserId = "";
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        MyFirebaseMessaging.inOthersProfile = true;
        MyFirebaseMessaging.inOthersProfileUserId = searchId;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifs = notificationManager.getActiveNotifications();
        if (notifs != null) if (notifs.length > 0) {
            for (int i = 0; i < notifs.length; i++) {
                if (notifs[i].getId() == 1) {
                    if (notifs[i].getNotification().extras != null) {
                        Bundle bundle = notifs[i].getNotification().extras;
                        if (bundle.get("userId").equals(searchId)) notificationManager.cancel(1);
                    }
                }
            }
        }
    }
}
