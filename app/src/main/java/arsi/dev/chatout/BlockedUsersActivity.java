package arsi.dev.chatout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import arsi.dev.chatout.adapters.BlockedUsersRecyclerAdapter;
import arsi.dev.chatout.cards.BlockedCard;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Locale;

public class BlockedUsersActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private BlockedUsersRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<BlockedCard> cards;
    private EditText search;
    private TextView noUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        search = findViewById(R.id.blocked_users_search);
        noUser = findViewById(R.id.blocked_users_nouser);

        cards = new ArrayList<>();

        recyclerView = findViewById(R.id.blocked_users_recycler_view);
        adapter = new BlockedUsersRecyclerAdapter(cards,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        getDataFromFirestore();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
    }


    private void filter(String text) {
        ArrayList<BlockedCard> filteredList = new ArrayList<>();
        if (!text.isEmpty() || !text.equals("")) {
            for (BlockedCard card : cards) {
                if (card.getUsername().toLowerCase(new Locale("tr","TR")).contains(text.toLowerCase(new Locale("tr","TR")))) {
                    filteredList.add(card);
                }
            }
            adapter.filterList(filteredList);
        } else {
            adapter.filterList(cards);
        }
    }

    private void getDataFromFirestore() {
        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                cards.clear();
                ArrayList<String> blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");

                if (blockedUsers.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noUser.setVisibility(View.VISIBLE);
                    cards = new ArrayList<>();
                    adapter.notifyDataSetChanged();
                } else {
                    noUser.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                for (int i = 0 ; i < blockedUsers.size() ; i++) {
                    String blockedUser = blockedUsers.get(i);
                    final int finalI = i;
                    DocumentReference documentReference1 = firebaseFirestore.collection("users").document(blockedUser);
                    documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String userId = documentSnapshot.getId();
                            String username = documentSnapshot.getString("username");
                            ArrayList<String> blockedByUsers = (ArrayList<String>) documentSnapshot.get("blockedByArray");

                            BlockedCard card = new BlockedCard(username,userId,blockedUsers,blockedByUsers,finalI);
                            cards.add(card);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
}
