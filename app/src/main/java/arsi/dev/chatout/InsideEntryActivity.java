package arsi.dev.chatout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import arsi.dev.chatout.adapters.InsideEntryRecyclerAdapter;
import arsi.dev.chatout.cards.InsideEntryCard;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InsideEntryActivity extends AppCompatActivity {

    private ArrayList<InsideEntryCard> cards;
    private InsideEntryRecyclerAdapter recyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private String entryId,titleText,commentText,commentId;
    private ArrayList<String> likesArray, dislikesArray, blockedUsers, blockedByUsers;
    private ImageView createComment,delete;
    private boolean isOwner,alreadyCommented;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside_entry);

        Intent intent = getIntent();
        entryId = intent.getStringExtra("entryId");

        createComment = findViewById(R.id.createComment);
        delete = findViewById(R.id.inside_entry_delete);
        title = findViewById(R.id.inside_entry_entry_title);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        likesArray = new ArrayList<>();
        dislikesArray = new ArrayList<>();
        cards = new ArrayList<>();
        blockedByUsers = new ArrayList<>();
        blockedUsers = new ArrayList<>();

        recyclerAdapter = new InsideEntryRecyclerAdapter(cards, this, entryId);
        RecyclerView recyclerView = findViewById(R.id.insideEntryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

        createComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!alreadyCommented) {
                    Intent intent = new Intent(InsideEntryActivity.this,CreateComment.class);
                    intent.putExtra("entryId",entryId);
                    intent.putExtra("titleText",titleText);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(InsideEntryActivity.this,CreateComment.class);
                    intent.putExtra("entryId",entryId);
                    intent.putExtra("titleText",titleText);
                    intent.putExtra("editing",true);
                    intent.putExtra("comment",commentText);
                    intent.putExtra("commentId",commentId);
                    startActivity(intent);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOwner) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InsideEntryActivity.this);
                    builder.setTitle("Dikkat");
                    builder.setMessage("Bu başlığı silmek istiyor musunuz?");
                    builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            HashMap<String,Object> userUpdate = new HashMap<>();
                            userUpdate.put("openedEntries", FieldValue.arrayRemove(entryId));
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                            documentReference.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    DocumentReference documentReference1 = firebaseFirestore.collection("entries").document(entryId);
                                    documentReference1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(InsideEntryActivity.this, "Başlık başarıyla silinmiştir.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    });
                    builder.show();
                }
            }
        });
        getDataFromFirestore();
    }

    private void getDataFromFirestore() {
        CollectionReference collectionReference = firebaseFirestore.collection("entries").document(entryId).collection("comments");

        collectionReference.orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                cards.clear();
                String uid = firebaseAuth.getCurrentUser().getUid();
                DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        blockedUsers = (ArrayList<String>) documentSnapshot.get("blockedArray");
                        blockedByUsers = (ArrayList<String>) documentSnapshot.get("blockedByArray");
                    }
                });
                DocumentReference documentReference1 = firebaseFirestore.collection("entries").document(entryId);
                documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String creatorUid = documentSnapshot.getString("creatorUid");
                        titleText = documentSnapshot.getString("title");

                        title.setText(titleText);

                        if (creatorUid.equals(uid)) {
                            isOwner = true;
                            delete.setVisibility(View.VISIBLE);
                        }
                    }
                });
                if (e != null) {
                    Toast.makeText(InsideEntryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                if (queryDocumentSnapshots != null) {
                    for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        String documentId = snapshot.getId();
                        Map<String, Object> data = snapshot.getData();
                        String comment = (String) data.get("commentMessage");
                        String senderUid = (String) data.get("sender");
                        String username = (String) data.get("senderUsername");
                        Timestamp timestamp = (Timestamp) data.get("time");
                        likesArray = (ArrayList<String>) data.get("likesArray");
                        dislikesArray = (ArrayList<String>) data.get("dislikesArray");

                        if (senderUid.equals(uid)) {
                            alreadyCommented = true;
                            commentText = comment;
                            commentId = documentId;
                        }

                        if(!blockedUsers.contains(senderUid) && !blockedByUsers.contains(senderUid)) {

                            InsideEntryCard card = new InsideEntryCard(comment, likesArray, dislikesArray, username, timestamp, documentId);
                            cards.add(card);

                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
