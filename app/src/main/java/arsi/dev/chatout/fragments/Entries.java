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

import arsi.dev.chatout.R;
import arsi.dev.chatout.adapters.EntriesInfoRecyclerAdapter;
import arsi.dev.chatout.cards.EntryInfoCard;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class Entries extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<EntryInfoCard> entryInfoCards;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Entries fragment;
    private EntriesInfoRecyclerAdapter recyclerAdapter;
    private String userId;

    public Entries(String userId){
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entries_info,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        entryInfoCards = new ArrayList<>();
        fragment = this;

        recyclerView = view.findViewById(R.id.entryInfoRecyclerView);
        recyclerAdapter = new EntriesInfoRecyclerAdapter(entryInfoCards,getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);

        getDataFromFirestore();

        return view;
    }

    private void getDataFromFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                entryInfoCards.clear();
                ArrayList<String> openedEntries = (ArrayList<String>) documentSnapshot.get("openedEntries");

                if (openedEntries.isEmpty()) {
                    entryInfoCards = new ArrayList<>();
                    recyclerAdapter.setType(entryInfoCards);
                }

                for (int i = 0 ; i < openedEntries.size() ; i++) {
                    String entryId = openedEntries.get(i);
                    final int finalI = i;

                    DocumentReference documentReference1 = firebaseFirestore.collection("entries").document(entryId);
                    documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Map<String, Object> data = documentSnapshot.getData();

                            if (data != null) {
                                String documentId = documentSnapshot.getId();
                                String title = (String) data.get("title");
                                Timestamp time = (Timestamp) data.get("time");

                                EntryInfoCard card = new EntryInfoCard(title,documentId,time,finalI);
                                entryInfoCards.add(card);
                                entryInfoCards.sort(new Comparator<EntryInfoCard>() {
                                    @Override
                                    public int compare(EntryInfoCard lhs, EntryInfoCard rhs) {
                                        return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                    }
                                });
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }
}
