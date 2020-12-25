package arsi.dev.chatout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class CreateEntryActivity extends AppCompatActivity {

    private EditText entryTitle, entryComment;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<String> likesArray, dislikesArray;
    private HashMap<String,Object> writtenComments;
    private String senderUsername;
    private GifImageView gifImageView;
    private Button save;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        save = findViewById(R.id.create_entry_save);
        gifImageView = findViewById(R.id.animation);
        entryTitle = findViewById(R.id.entryText);
        entryComment = findViewById(R.id.entryComment);
        relativeLayout = findViewById(R.id.create_entry_relative);

        likesArray = new ArrayList<>();
        dislikesArray = new ArrayList<>();
        writtenComments = new HashMap<>();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save.setClickable(false);
                String uid = firebaseAuth.getCurrentUser().getUid();
                String comment = entryComment.getText().toString().trim();
                String title = entryTitle.getText().toString().trim();

                if (title.isEmpty()) {
                    save.setClickable(true);
                    Toast.makeText(CreateEntryActivity.this, "Lütfen bir başlık giriniz.", Toast.LENGTH_SHORT).show();
                } else {
                    if (title.length() < 5) {
                        save.setClickable(true);
                        Toast.makeText(CreateEntryActivity.this, "Başlık 5 karakterden daha kısa olamaz.", Toast.LENGTH_SHORT).show();
                    }
                    Date pushDate = new Date();
                    Timestamp timestamp = new Timestamp(pushDate);

                    HashMap<String,Object> data = new HashMap<>();
                    data.put("time", timestamp);
                    data.put("title",title);
                    data.put("creatorUid",uid);
                    if (comment.isEmpty()) data.put("numberOfComments",0);
                    else data.put("numberOfComments",1);
                    data.put("lastCommentTime",timestamp);

                    save.setVisibility(View.GONE);
                    entryComment.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.GONE);
                    entryTitle.setVisibility(View.GONE);
                    gifImageView.setVisibility(View.VISIBLE);

                    CollectionReference collectionReference = firebaseFirestore.collection("entries");
                    collectionReference.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(final DocumentReference documentReference){
                            String entryId = documentReference.getId();
                            if (!comment.isEmpty()) {

                                Date pushDate = new Date(); // don't forget if.
                                Timestamp timestamp1 = new Timestamp(pushDate);
                                HashMap<String,Object> data1 = new HashMap<>();
                                data1.put("sender",uid);
                                data1.put("time",timestamp1);
                                data1.put("senderUsername",senderUsername);
                                data1.put("commentMessage",comment);
                                data1.put("likesArray",likesArray);
                                data1.put("dislikesArray",dislikesArray);

                                CollectionReference collectionReference1 = firebaseFirestore.collection("entries").document(entryId).collection("comments");
                                collectionReference1.add(data1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        String commentId = documentReference.getId();
                                        writtenComments.put(commentId,entryId);
                                        HashMap<String,Object> userUpdate = new HashMap<>();
                                        userUpdate.put("openedEntries", FieldValue.arrayUnion(entryId));
                                        userUpdate.put("writtenComments",writtenComments);
                                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
                                        documentReference1.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent i = new Intent(CreateEntryActivity.this,InsideEntryActivity.class);
                                                        i.putExtra("entryId",entryId);
                                                        startActivity(i);
                                                        finish();
                                                    }
                                                }, 3000);
                                            }
                                        });
                                    }
                                });
                            } else {
                                HashMap<String,Object> userUpdate = new HashMap<>();
                                userUpdate.put("openedEntries",FieldValue.arrayUnion(entryId));
                                DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
                                documentReference1.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent i = new Intent(CreateEntryActivity.this,InsideEntryActivity.class);
                                                i.putExtra("entryId",entryId);
                                                startActivity(i);
                                                finish();
                                            }
                                        }, 3000);
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            save.setClickable(true);
                        }
                    });
                }
            }
        });

        getDataFromFirestore();
    }

    public void getDataFromFirestore(){
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(CreateEntryActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                if(documentSnapshot != null){
                    Map<String,Object> data = documentSnapshot.getData();
                    senderUsername = (String) data.get("username");
                    writtenComments = (HashMap<String,Object>) data.get("writtenComments");
                }
            }
        });
    }

}
