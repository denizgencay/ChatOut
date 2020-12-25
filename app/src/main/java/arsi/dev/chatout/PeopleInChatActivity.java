package arsi.dev.chatout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


import arsi.dev.chatout.adapters.PeopleInChatRecyclerAdapter;
import arsi.dev.chatout.cards.SearchCard;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Comparator;

public class PeopleInChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<SearchCard> people;
    private PeopleInChatRecyclerAdapter recyclerAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String chatId;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_in_chat);

        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");

        back = findViewById(R.id.people_in_chat_back);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        people = new ArrayList<>();

        recyclerView = findViewById(R.id.people_in_chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new PeopleInChatRecyclerAdapter(people,this,chatId);
        recyclerView.setAdapter(recyclerAdapter);

        getDataFromFirestore();
        getMyInfo();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getMyInfo() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                ArrayList<String> blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");
                recyclerAdapter.setBlockedUsers(blockedUsers);
            }
        });
    }

    private void getDataFromFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("chats").document(chatId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                people.clear();

                ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");
                String creatorUid = documentSnapshot.getString("creatorUid");

                if (peopleInChat != null) {
                    for (int i = 0 ; i < peopleInChat.size() ; i++) {
                        String userUid = peopleInChat.get(i);
                        final int finalI = i;

                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(userUid);
                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String username = (String) documentSnapshot.getString("username");
                                String photoUri = (String) documentSnapshot.getString("photoUri");
                                String pushToken = (String) documentSnapshot.getString("pushToken");
                                String userId = documentSnapshot.getId();

                                SearchCard card = new SearchCard(username,userId,photoUri,creatorUid,pushToken,finalI);
                                people.add(card);

                                people.sort(new Comparator<SearchCard>() {
                                    @Override
                                    public int compare(SearchCard lhs, SearchCard rhs) {
                                        return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                    }
                                });

                                recyclerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }
}
