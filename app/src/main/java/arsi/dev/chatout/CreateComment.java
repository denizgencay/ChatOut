package arsi.dev.chatout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreateComment extends AppCompatActivity {

    private EditText comment;
    private TextView title;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private String commentText,titleText,entryId,commentTextFromEdit,commentId;
    private Button save;
    private InterstitialAd interstitialAd;
    private boolean isEditing,errorGiven;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_comment);

        comment = findViewById(R.id.create_comment_comment);
        save = findViewById(R.id.create_comment_send);
        title = findViewById(R.id.create_comment_title);

        Intent intent = getIntent();
        entryId = intent.getStringExtra("entryId");
        titleText = intent.getStringExtra("titleText");
        isEditing = intent.getBooleanExtra("editing",false);

        if (isEditing) {
            commentTextFromEdit = intent.getStringExtra("comment");
            comment.setText(commentTextFromEdit);
            commentId = intent.getStringExtra("commentId");
        }

        errorGiven = false;

        title.setText(titleText);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        interstitialAd = new InterstitialAd(CreateComment.this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save.setClickable(false);
                String uid = firebaseAuth.getCurrentUser().getUid();
                commentText = comment.getText().toString().trim();

                if (isEditing) {
                    String message = comment.getText().toString();
                    if (!message.isEmpty()) {
                        HashMap<String,Object> update = new HashMap<>();
                        update.put("commentMessage",message);

                        DocumentReference documentReference = firebaseFirestore.collection("entries").document(entryId).collection("comments").document(commentId);
                        documentReference.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CreateComment.this, "Yorumunuz başarıyla güncellenmiştir.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    } else {
                        if (!errorGiven) {
                            save.setClickable(true);
                            Toast.makeText(CreateComment.this, "Lütfen yorum kısmını doldurunuz.", Toast.LENGTH_SHORT).show();
                            errorGiven = true;
                        }
                    }
                } else {
                    if (!commentText.isEmpty()) {
                        Date pushDate = new Date();
                        Timestamp timestamp = new Timestamp(pushDate);

                        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                HashMap<String,Object> writtenComments = (HashMap<String, Object>) documentSnapshot.get("writtenComments");
                                String username = (String) documentSnapshot.getString("username");
                                Boolean isPremium = (Boolean) documentSnapshot.getBoolean("isPremium");

                                HashMap<String,Object> data1 = new HashMap<>();
                                data1.put("sender",uid);
                                data1.put("time",timestamp);
                                data1.put("commentMessage",commentText);
                                data1.put("senderUsername",username);
                                data1.put("dislikesArray",new ArrayList<>());
                                data1.put("likesArray",new ArrayList<>());

                                DocumentReference documentReference1 = firebaseFirestore.collection("entries").document(entryId);
                                CollectionReference collectionReference = documentReference1.collection("comments");
                                collectionReference.add(data1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference2) {
                                        HashMap<String,Object> entryUpdate = new HashMap<>();
                                        entryUpdate.put("numberOfComments", FieldValue.increment(1));
                                        entryUpdate.put("lastCommentTime",timestamp);
                                        documentReference1.set(entryUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                writtenComments.put(documentReference2.getId(),entryId);
                                                HashMap<String,Object> update = new HashMap<>();
                                                update.put("writtenComments",writtenComments);
                                                documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (!isPremium) {
                                                            if (interstitialAd.isLoaded()) {
                                                                interstitialAd.show();
                                                            } else {
                                                                Toast.makeText(CreateComment.this, "Ad not loaded", Toast.LENGTH_SHORT).show();
                                                            }
                                                            interstitialAd.setAdListener(new AdListener() {
                                                                @Override
                                                                public void onAdClosed() {
                                                                    super.onAdClosed();
                                                                    finish();
                                                                }
                                                            });
                                                        } else {
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } else {
                        if (!errorGiven) {
                            save.setClickable(true);
                            Toast.makeText(CreateComment.this, "Lütfen yorum kısmını doldurunuz.", Toast.LENGTH_SHORT).show();
                            errorGiven = true;
                        }
                    }
                }
            }
        });
    }
}
