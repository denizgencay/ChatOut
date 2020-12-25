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
import arsi.dev.chatout.adapters.CommentsRecyclerAdapter;
import arsi.dev.chatout.cards.CommentCard;
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
import java.util.HashMap;
import java.util.List;

public class Comments extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<CommentCard> commentCards;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Comments fragment;
    private CommentsRecyclerAdapter recyclerAdapter;
    private String userId;

    public Comments(String userId) {
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_info,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        commentCards = new ArrayList<>();

        fragment = this;

        recyclerView = view.findViewById(R.id.commentRecyclerView);
        recyclerAdapter = new CommentsRecyclerAdapter(commentCards,getContext());
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
               commentCards.clear();
               HashMap<String, Object> writtenComments = (HashMap<String, Object>) documentSnapshot.get("writtenComments");

               if (writtenComments.size() == 0) {
                   commentCards = new ArrayList<>();
                   recyclerAdapter.setType(commentCards);
               }

               List<String> keys = new ArrayList<>(writtenComments.keySet());
               for (int i = 0 ; i < keys.size() ; i++) {
                   String comment = keys.get(i);
                   String key = writtenComments.get(comment).toString();
                   final int finalI = i;

                   DocumentReference documentReference1 = firebaseFirestore.collection("entries").document(key);
                   documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                       @Override
                       public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String title = (String) documentSnapshot.getString("title");
                            System.out.println(comment);
                            DocumentReference documentReference2 = firebaseFirestore.collection("entries").document(key).collection("comments").document(comment);
                            documentReference2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String message = (String) documentSnapshot.getString("commentMessage");

                                    CommentCard card = new CommentCard(message,title,finalI,writtenComments,comment);
                                    commentCards.add(card);
                                    commentCards.sort(new Comparator<CommentCard>() {
                                        @Override
                                        public int compare(CommentCard lhs, CommentCard rhs) {
                                            return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                        }
                                    });

                                    recyclerAdapter.notifyDataSetChanged();
                                }
                            });
                       }
                   });
               }
           }
       });
    }
}
