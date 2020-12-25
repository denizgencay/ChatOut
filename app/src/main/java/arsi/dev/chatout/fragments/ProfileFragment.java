package arsi.dev.chatout.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import arsi.dev.chatout.Authentication;
import arsi.dev.chatout.EntryInfoActivity;
import arsi.dev.chatout.FollowersActivity;
import arsi.dev.chatout.FollowingActivity;
import arsi.dev.chatout.MessageActivity;
import arsi.dev.chatout.ProfileEdit;
import arsi.dev.chatout.R;
import arsi.dev.chatout.Settings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private TextView usernameText, aboutMeText, followersText, followingText;
    private ImageView profilePicture;
    private ArrayList<String> followers, following;
    private ImageView profileEditAction, settingsAction;
    private RelativeLayout chatInfo,aboutMeView,penManView;
    private TextView chatTitle,chatNumberOfPerson,chatFinishTime;
    private Button goToChat,goToDict;
    private boolean isPremium;

    private final int SETTINGS_RESULT = 0;
    private final int EDIT_RESULT = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        usernameText = view.findViewById(R.id.usernameText);
        profilePicture = view.findViewById(R.id.profilePicture);
        aboutMeText = view.findViewById(R.id.aboutMe);
        followersText = view.findViewById(R.id.followersText);
        followingText = view.findViewById(R.id.followingText);
        profileEditAction = view.findViewById(R.id.profileEditAction);
        settingsAction = view.findViewById(R.id.settingsAction);
        chatInfo = view.findViewById(R.id.profile_chat_info);
        aboutMeView = view.findViewById(R.id.profile_about_me_relative);
        penManView = view.findViewById(R.id.profile_pencil_man_relative);
        chatTitle = view.findViewById(R.id.profile_chat_title);
        chatNumberOfPerson = view.findViewById(R.id.profile_attendants);
        chatFinishTime = view.findViewById(R.id.profile_end_time);
        goToChat = view.findViewById(R.id.profile_go_to_chat);
        goToDict = view.findViewById(R.id.profile_go_to_dict);

        followers = new ArrayList<>();
        following = new ArrayList<>();

        profilePicture.setClickable(false);

        profileEditAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileEdit.class);
                startActivityForResult(intent,EDIT_RESULT);
            }
        });

        settingsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getContext(), Settings.class);
                settingsIntent.putExtra("premium",isPremium);
                startActivityForResult(settingsIntent,SETTINGS_RESULT);
            }
        });

        followersText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getContext(), FollowersActivity.class);
                startActivity(settingsIntent);
            }
        });

        followingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getContext(), FollowingActivity.class);
                startActivity(settingsIntent);
            }
        });

        goToDict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EntryInfoActivity.class);
                intent.putExtra("userId",firebaseAuth.getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        getDataFromFireStore();
        getMyProfilePhoto();

        return view;
    }

    private void getDataFromFireStore() {
        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                if(documentSnapshot != null){
                    Map<String,Object> data = documentSnapshot.getData();
                    String username = (String) data.get("username");
                    String aboutMe = (String) data.get("aboutMe");
                    String activeChat = (String) data.get("activeChat");
                    ArrayList<String> followers = (ArrayList<String>) data.get("followers");
                    ArrayList<String> following = (ArrayList<String>) data.get("followings");
                    isPremium = (boolean) data.get("isPremium");

                    followersText.setText(String.valueOf(followers.size()));
                    followingText.setText(String.valueOf(following.size()));
                    aboutMeText.setText(aboutMe);

                    if (!aboutMe.isEmpty()) {
                        penManView.setVisibility(View.GONE);
                        aboutMeView.setVisibility(View.VISIBLE);
                    } else {
                        aboutMeView.setVisibility(View.GONE);
                    }

                    if (!activeChat.isEmpty()) {
                        penManView.setVisibility(View.GONE);
                        chatInfo.setVisibility(View.VISIBLE);

                        DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(activeChat);
                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
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

                                chatTitle.setText(title);
                                chatNumberOfPerson.setText(numberOfPersonText);
                                chatFinishTime.setText(timeText);
                                goToChat.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(getContext(), MessageActivity.class);
                                        intent.putExtra("chatId",activeChat);
                                        startActivity(intent);
                                    }
                                });
                            }
                        });
                    } else {
                        chatInfo.setVisibility(View.GONE);
                    }

                    if (aboutMe.isEmpty() && activeChat.isEmpty()) {
                        penManView.setVisibility(View.VISIBLE);
                    }

                    usernameText.setText(username);
                }
            }
        });
    }

    private void getMyProfilePhoto() {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    String photoUri = documentSnapshot.getString("photoUri");

                    Picasso.get().load(photoUri).noFade().into(profilePicture);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SETTINGS_RESULT) {
            Intent intent = new Intent(getContext(), Authentication.class);
            startActivity(intent);
            getActivity().finish();
        } else if (resultCode == EDIT_RESULT) {
            getMyProfilePhoto();
        }
    }
}
