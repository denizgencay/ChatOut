package arsi.dev.chatout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class EntryCommentActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String entryId, commentId, messageText, titleText;
    private TextView title, message;
    private ImageView delete, edit;
    private Boolean isOwner;
    private ListenerRegistration task1, task2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_comment);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        isOwner = false;

        title = findViewById(R.id.entry_comment_title);
        message = findViewById(R.id.entry_comment_message);
        delete = findViewById(R.id.entry_comment_delete);
        edit = findViewById(R.id.entry_comment_edit);

        Intent intent = getIntent();
        entryId = intent.getStringExtra("entryId");
        commentId = intent.getStringExtra("commentId");

        getDataFromFirestore();

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOwner) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EntryCommentActivity.this);
                    builder.setTitle("Dikkat");
                    builder.setMessage("Bu yorumu silmek istiyor musunuz?");
                    builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            task1.remove();
                            String myUid = firebaseAuth.getCurrentUser().getUid();

                            DocumentReference documentReference = firebaseFirestore.collection("users").document(myUid);
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    HashMap<String, Object> writtenComments = (HashMap<String, Object>) documentSnapshot.get("writtenComments");
                                    writtenComments.remove(commentId);

                                    HashMap<String, Object> update = new HashMap<>();
                                    update.put("writtenComments", writtenComments);

                                    documentReference.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            HashMap<String, Object> entryUpdate = new HashMap<>();
                                            entryUpdate.put("numberOfComments", FieldValue.increment(-1));

                                            DocumentReference documentReference1 = firebaseFirestore.collection("entries").document(entryId);
                                            documentReference1.set(entryUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    documentReference1.collection("comments").document(commentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(EntryCommentActivity.this, "Yorumunuz kaldırıldı.", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOwner) {
                    Intent intent = new Intent(EntryCommentActivity.this, CreateComment.class);
                    intent.putExtra("commentId", commentId);
                    intent.putExtra("entryId", entryId);
                    intent.putExtra("comment", messageText);
                    intent.putExtra("editing", true);
                    intent.putExtra("titleText", titleText);
                    startActivity(intent);
                }
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EntryCommentActivity.this, InsideEntryActivity.class);
                intent.putExtra("entryId", entryId);
                startActivity(intent);
            }
        });
    }

    private void getDataFromFirestore() {
        String myUid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("entries").document(entryId);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                titleText = documentSnapshot.getString("title");
                title.setText(titleText);
                task1 = documentReference.collection("comments").document(commentId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        messageText = documentSnapshot.getString("commentMessage");
                        String senderUid = documentSnapshot.getString("sender");

                        if (senderUid.equals(myUid)) {
                            delete.setVisibility(View.VISIBLE);
                            edit.setVisibility(View.VISIBLE);
                            isOwner = true;
                        }
                        message.setText(messageText);
                    }
                });
            }
        });
    }
}
