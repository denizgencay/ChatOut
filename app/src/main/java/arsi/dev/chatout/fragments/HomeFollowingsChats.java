package arsi.dev.chatout.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Map;

import arsi.dev.chatout.R;
import arsi.dev.chatout.adapters.RecyclerAdapter;
import arsi.dev.chatout.cards.ChatCard;

public class HomeFollowingsChats extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ArrayList<ChatCard> cards;
    private String activeChat1;
    private ArrayList<String> followings, blockedUsers;

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private boolean isPremium;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_followings_chats, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        cards = new ArrayList<>();
        followings = new ArrayList<>();
        blockedUsers = new ArrayList<>();
        activeChat1 = "";
        isPremium = false;

        recyclerView = view.findViewById(R.id.followings_chats_recycler_view);
        recyclerAdapter = new RecyclerAdapter(cards, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);

        return view;
    }

    private void getDataFromFirestore() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.get("followings") != null)
                    followings = (ArrayList<String>) documentSnapshot.get("followings");
                else
                    followings = new ArrayList<>();
                if (documentSnapshot.get("blockedArray") != null)
                    blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");
                else
                    blockedUsers = new ArrayList<>();
                if (documentSnapshot.getString("activeChat") != null)
                    activeChat1 = documentSnapshot.getString("activeChat");
                else activeChat1 = "";
                if (documentSnapshot.getBoolean("isPremium") != null)
                    isPremium = documentSnapshot.getBoolean("isPremium");
                else isPremium = false;

                if (!activeChat1.isEmpty()) {
                    checkIfCreator();
                }

                if (followings.isEmpty()) {
                    recyclerAdapter.setType(new ArrayList<>());
                }

                CollectionReference collectionReference = firebaseFirestore.collection("chats");

                collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        cards.clear();
                        if (queryDocumentSnapshots != null) {
                            int j = 0;
                            for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                                DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(i);
                                if (followings.contains(snapshot.getString("creatorUid"))) {
                                    String documentId = snapshot.getId();
                                    Map<String, Object> data = snapshot.getData();
                                    String title = (String) data.get("title");
                                    String personNumber = (String) data.get("numberOfPerson");
                                    Timestamp finishTime = (Timestamp) data.get("finishTime");
                                    ArrayList<String> peopleInChat = (ArrayList<String>) data.get("peopleInChat");
                                    ArrayList<String> chatBlockedUsers = (ArrayList<String>) data.get("blockedUsers");

                                    if (!chatBlockedUsers.contains(uid)) {
                                        if (snapshot.getId().equals(activeChat1)) {
                                            ChatCard card = new ChatCard(finishTime, personNumber, title, documentId, blockedUsers, peopleInChat, activeChat1);
                                            cards.add(0, card);
                                        } else {
                                            ChatCard card = new ChatCard(finishTime, personNumber, title, documentId, blockedUsers, peopleInChat, activeChat1);
                                            cards.add(card);
                                        }
                                    }
                                    j++;
                                }
                            }

                            for (int i = 0; i < j + 1; i++) {
                                if (i != 0 && i % 4 == 0 && !isPremium) {
                                    ChatCard card1 = new ChatCard();
                                    cards.add(i, card1);
                                }
                            }

                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

    private void checkIfCreator() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(activeChat1);
        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String creatorUid = documentSnapshot.getString("creatorUid");
                ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");

                if (creatorUid.equals(uid) && peopleInChat.size() > 1)
                    recyclerAdapter.setCreator(true);
                else recyclerAdapter.setCreator(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataFromFirestore();
    }
}
