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

import arsi.dev.chatout.adapters.FollowersRecyclerAdapter;
import arsi.dev.chatout.cards.FollowersCard;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class FollowersActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<FollowersCard> cards;
    private FollowersRecyclerAdapter recyclerAdapter;
    private EditText search;
    private TextView text;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        search = findViewById(R.id.followers_search);
        text = findViewById(R.id.followers_text);

        cards = new ArrayList<>();

        recyclerAdapter  = new FollowersRecyclerAdapter(this,cards);
        recyclerView = findViewById(R.id.followersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

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
        ArrayList<FollowersCard> filteredList = new ArrayList<>();
        if (!text.isEmpty() || !text.equals("")) {
            for (FollowersCard card : cards) {
                if (card.getUsername().toLowerCase(new Locale("tr","TR")).contains(text.toLowerCase(new Locale("tr","TR")))) {
                    filteredList.add(card);
                }
            }
            recyclerAdapter.filterList(filteredList);
        } else {
            recyclerAdapter.filterList(cards);
        }
    }

    public void getDataFromFirestore() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                cards.clear();
                Map<String,Object> data = documentSnapshot.getData();
                ArrayList<String> myFollowers = (ArrayList<String>) data.get("followers");

                if (myFollowers.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    text.setVisibility(View.VISIBLE);
                    cards = new ArrayList<>();
                    recyclerAdapter.notifyDataSetChanged();
                } else {
                    text.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                    for (int i = 0; i < myFollowers.size(); i++ ){
                        final int finalI = i;
                        String followerUid = myFollowers.get(i);
                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(followerUid);
                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String username = documentSnapshot.getString("username");
                                String photoUri = documentSnapshot.getString("photoUri");
                                String followersId = documentSnapshot.getString("userId");

                                FollowersCard card = new FollowersCard(username, followersId ,photoUri ,finalI);

                                cards.add(card);
                                cards.sort(new Comparator<FollowersCard>() {
                                    @Override
                                    public int compare(FollowersCard lhs, FollowersCard rhs) {
                                        return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                    }
                                });
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
            }
        });
    }
}
