package arsi.dev.chatout.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.R;
import arsi.dev.chatout.adapters.EntryRecyclerAdapter;
import arsi.dev.chatout.cards.EntryCard;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class FollowingEntries extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private RecyclerView recyclerView;
    private ArrayList<EntryCard> cards;
    private EntryRecyclerAdapter recyclerAdapter;
    private ArrayList<String> blockedUsers, blockedByUsers, followings;
    private EditText search;
    private boolean isPremium;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entries_following,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        search = view.findViewById(R.id.following_entries_search);

        cards = new ArrayList<>();
        blockedByUsers = new ArrayList<>();
        blockedUsers = new ArrayList<>();
        followings = new ArrayList<>();

        recyclerView = view.findViewById(R.id.followingEntriesRecyclerView);
        recyclerAdapter = new EntryRecyclerAdapter(cards,getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

        return view;
    }

    private void filter(String text) {
        ArrayList<EntryCard> filteredList = new ArrayList<>();
        if (!text.isEmpty() || !text.equals("")) {
            for (EntryCard card : cards) {
                if (card.getTitle().toLowerCase(new Locale("tr","Tr")).contains(text.toLowerCase(new Locale("tr","Tr")))) {
                    filteredList.add(card);
                }
            }
            recyclerAdapter.filterList(filteredList);
        } else {
            recyclerAdapter.filterList(cards);
        }
    }

    public void getDataFromFirestore() {

        CollectionReference collectionReference = firebaseFirestore.collection("entries");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");
                        blockedByUsers = (ArrayList<String>) documentSnapshot.get("blockedByArray");
                        followings = (ArrayList<String>) documentSnapshot.get("followings");
                        isPremium = documentSnapshot.getBoolean("isPremium");
                    }
                });

                cards.clear();
                if(e != null){
                    Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }

                if(queryDocumentSnapshots != null){
                    for(int i = 0 ; i < queryDocumentSnapshots.getDocuments().size() ; i++) {
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(i);
                        Map<String, Object> data = snapshot.getData();
                        if (data != null) {
                            String documentId = snapshot.getId();
                            String title = (String) data.get("title");
                            String creatorUid = (String) data.get("creatorUid");
                            Timestamp time = (Timestamp) data.get("time");
                            long size = (long) data.get("numberOfComments");
                            String commentNumber = String.valueOf(size);

                            if(!blockedByUsers.contains(creatorUid) && !blockedUsers.contains(creatorUid)){
                                if(followings.contains(creatorUid)){
                                    EntryCard card = new EntryCard(title, commentNumber, documentId,time);
                                    cards.add(card);
                                    System.out.println();
                                    if (i != 0 && i % 3 == 0 && !isPremium) {
                                        EntryCard ad = new EntryCard();
                                        cards.add(ad);
                                    }
                                    recyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
        });

    }
}
