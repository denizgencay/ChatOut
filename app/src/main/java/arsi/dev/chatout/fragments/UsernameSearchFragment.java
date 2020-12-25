package arsi.dev.chatout.fragments;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import arsi.dev.chatout.R;
import arsi.dev.chatout.adapters.SearchRecyclerAdapter;
import arsi.dev.chatout.cards.SearchCard;

public class UsernameSearchFragment extends Fragment {

    private final int OTHERS_PROFILE_RESULT = 1;
    private ArrayList<SearchCard> searchCards, lastSearch;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private EditText searchText;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private ArrayList<String> blockedArray;
    private UsernameSearchFragment fragment;
    private boolean firstFocus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_username_search, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        searchText = view.findViewById(R.id.searchView);

        searchCards = new ArrayList<>();
        blockedArray = new ArrayList<>();
        lastSearch = new ArrayList<>();

        fragment = this;
        firstFocus = true;

        searchRecyclerAdapter = new SearchRecyclerAdapter(searchCards, getContext(), fragment, searchText);
        RecyclerView recyclerView = view.findViewById(R.id.searchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchRecyclerAdapter);

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
                    searchRecyclerAdapter.setType(lastSearch);
                    firstFocus = false;
                }
            }
        });

        getDataFromFireStore();

        return view;
    }

    private void filter(String text) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        ArrayList<SearchCard> filteredList = new ArrayList<>();
        if (!text.isEmpty() || !text.equals("")) {
            for (SearchCard user : searchCards) {
                if (user.getUsername().toLowerCase(new Locale("tr", "TR")).contains(text.toLowerCase(new Locale("tr", "TR")))) {
                    if (!user.getSearchIdUsername().equals(uid)) {
                        filteredList.add(user);
                    }
                }
            }
            searchRecyclerAdapter.filterList(filteredList);
        } else {
            searchRecyclerAdapter.setType(lastSearch);
        }
    }

    private void getDataFromFireStore() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                lastSearch.clear();
                ArrayList<String> blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");
                ArrayList<String> blockedByUsers = (ArrayList<String>) documentSnapshot.get("blockedByArray");
                ArrayList<String> lastSearchs = (ArrayList<String>) documentSnapshot.get("lastSearchs");

                if (lastSearchs != null) {
                    Collections.reverse(lastSearchs);
                    searchRecyclerAdapter.setLastSearchUids(lastSearchs);

                    for (int i = 0; i < lastSearchs.size(); i++) {
                        String lastSearchUid = lastSearchs.get(i);
                        final int finalI = i;

                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(lastSearchUid);
                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String documentId = documentSnapshot.getId();
                                String username = (String) documentSnapshot.getString("username");
                                String photoUri = (String) documentSnapshot.getString("photoUri");

                                SearchCard lastSearchCard = new SearchCard(username, documentId, photoUri, finalI);
                                lastSearch.add(lastSearchCard);

                                lastSearch.sort(new Comparator<SearchCard>() {
                                    @Override
                                    public int compare(SearchCard lhs, SearchCard rhs) {
                                        return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                    }
                                });
                                searchRecyclerAdapter.setType(lastSearch);
                            }
                        });
                    }
                }

                CollectionReference collectionReference = firebaseFirestore.collection("users");
                collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        searchCards.clear();
                        if (e != null) {
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                String documentId = snapshot.getId();
                                Map<String, Object> data = snapshot.getData();
                                String username = (String) data.get("username");
                                String photoUri = (String) data.get("photoUri");
                                boolean isVerified;
                                if (data.get("isVerified") != null)
                                    isVerified = (boolean) data.get("isVerified");
                                else isVerified = false;

                                if (!blockedUsers.contains(documentId) && !documentId.equals(uid) && !blockedByUsers.contains(documentId) && isVerified) {
                                    SearchCard card = new SearchCard(username, documentId, photoUri);
                                    searchCards.add(card);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OTHERS_PROFILE_RESULT) {
            getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
        }
    }
}
