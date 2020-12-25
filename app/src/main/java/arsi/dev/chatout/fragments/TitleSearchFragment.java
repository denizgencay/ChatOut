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
import androidx.fragment.app.FragmentTransaction;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Map;

import arsi.dev.chatout.R;
import arsi.dev.chatout.adapters.SearchTitleRecyclerAdapter;
import arsi.dev.chatout.cards.SearchTitleCard;

public class TitleSearchFragment extends Fragment {

    private ArrayList<SearchTitleCard> searchTitleCards;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private EditText searchText;
    private SearchTitleRecyclerAdapter searchTitleRecyclerAdapter;
    private boolean firstFocus;
    private String userActiveChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_title_search, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        searchText = view.findViewById(R.id.searchViewTitle);

        searchTitleCards = new ArrayList<>();

        firstFocus = true;

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        searchTitleRecyclerAdapter = new SearchTitleRecyclerAdapter(searchTitleCards, getContext(), this, fragmentTransaction,searchText);
        RecyclerView recyclerView = view.findViewById(R.id.searchRecyclerViewTitle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchTitleRecyclerAdapter);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (firstFocus && hasFocus) {
                    searchTitleRecyclerAdapter.setType(new ArrayList<>());
                    firstFocus = false;
                }
            }
        });

        return view;
    }

    private void filter(String text) {
        ArrayList<SearchTitleCard> filteredList = new ArrayList<>();
        if (!text.isEmpty() || !text.equals("")) {
            for (SearchTitleCard chat : searchTitleCards) {
                if (chat.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(chat);
                }
            }
            searchTitleRecyclerAdapter.filterList(filteredList);
        } else {
            searchTitleRecyclerAdapter.setType(new ArrayList<>());
        }
    }

    public void getDataFromFireStore() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                ArrayList<String> blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");
                ArrayList<String> blockedByUsers = (ArrayList<String>) documentSnapshot.get("blockedByArray");
                userActiveChat = documentSnapshot.getString("activeChat");

                if (!userActiveChat.isEmpty()) {
                    checkIfCreator();
                }

                CollectionReference collectionReference = firebaseFirestore.collection("chats");

                collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        searchTitleCards.clear();
                        if (e != null) {
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                String creatorUid = snapshot.getString("creatorUid");
                                if (!blockedUsers.contains(creatorUid) && !blockedByUsers.contains(creatorUid)) {
                                    Map<String, Object> data = snapshot.getData();
                                    String documentId = snapshot.getId();
                                    String title = (String) data.get("title");
                                    String personNumber = (String) data.get("numberOfPerson");
                                    Timestamp finishTime = (Timestamp) data.get("finishTime");
                                    ArrayList<String> peopleInChat = (ArrayList<String>) data.get("peopleInChat");
                                    ArrayList<String> chatBlockedUsers = (ArrayList<String>) data.get("blockedUsers");

                                    if (!chatBlockedUsers.contains(uid)) {
                                        SearchTitleCard card = new SearchTitleCard(title, documentId, personNumber, finishTime, peopleInChat, blockedUsers, userActiveChat);
                                        searchTitleCards.add(card);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private void checkIfCreator() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(userActiveChat);
        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String creatorUid = documentSnapshot.getString("creatorUid");
                ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");

                if (creatorUid.equals(uid) && peopleInChat.size() > 1)
                    searchTitleRecyclerAdapter.setCreator(true);
                else searchTitleRecyclerAdapter.setCreator(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataFromFireStore();
    }
}
