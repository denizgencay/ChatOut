package arsi.dev.chatout;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import arsi.dev.chatout.adapters.ChangeModeratorRecyclerAdapter;
import arsi.dev.chatout.cards.ChangeModeratorCard;

public class ChangeModeratorActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private ChangeModeratorRecyclerAdapter recyclerAdapter;
    private ArrayList<ChangeModeratorCard> people;
    private HashMap<String, Object> keys;
    private String chatId, newChatId;
    private boolean fromInsideChat, fromCreateChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_moderator);

        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");
        newChatId = intent.getStringExtra("newChatId");
        fromInsideChat = intent.getBooleanExtra("insideChat", false);
        fromCreateChat = intent.getBooleanExtra("createChat", false);
        keys = (HashMap<String, Object>) intent.getSerializableExtra("keys");

        people = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.change_moderator_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new ChangeModeratorRecyclerAdapter(people, this, chatId, this, fromInsideChat, newChatId, fromCreateChat, keys);
        recyclerView.setAdapter(recyclerAdapter);

        getDataFromFirestore();
        getMyInfo();

    }

    private void getMyInfo() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                ArrayList<String> blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");
                boolean isPremium = documentSnapshot.getBoolean("isPremium");
                String myUsername = documentSnapshot.getString("username");

                recyclerAdapter.setBlockedUsers(blockedUsers);
                recyclerAdapter.setPremium(isPremium);
                recyclerAdapter.setMyUsername(myUsername);
            }
        });
    }

    private void getDataFromFirestore() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("chats").document(chatId);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                people.clear();

                ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");

                for (int i = 0; i < peopleInChat.size(); i++) {
                    String personUid = peopleInChat.get(i);
                    final int finalI = i;

                    DocumentReference documentReference1 = firebaseFirestore.collection("users").document(personUid);

                    documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String userId = documentSnapshot.getId();
                            if (!userId.equals(uid)) {
                                String photoUri = documentSnapshot.getString("photoUri");
                                String username = documentSnapshot.getString("username");

                                ChangeModeratorCard card = new ChangeModeratorCard(photoUri, username, userId, finalI);
                                people.add(card);

                                people.sort(new Comparator<ChangeModeratorCard>() {
                                    @Override
                                    public int compare(ChangeModeratorCard lhs, ChangeModeratorCard rhs) {
                                        return lhs.getPriority() < rhs.getPriority() ? -1 : lhs.getPriority() > rhs.getPriority() ? 1 : 0;
                                    }
                                });
                            }
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

    }
}
