package arsi.dev.chatout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView resend,deleteAccount;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        resend = findViewById(R.id.email_verification_resend);
        deleteAccount = findViewById(R.id.email_verification_delete_account);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("abow");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                });
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount.setClickable(false);
                String uid = firebaseUser.getUid();
                String email = firebaseUser.getEmail();
                DocumentReference documentReference1 = firebaseFirestore.collection("Passwords").document(uid);
                documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String password = (String) documentSnapshot.getString("password");
                        AuthCredential authCredential = EmailAuthProvider.getCredential(email,password);

                        firebaseUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                App.deleteAccount = true;
                                DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                DocumentReference documentReference2 = firebaseFirestore.collection("Passwords").document(uid);
                                                documentReference2.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        DocumentReference documentReference3 = firebaseFirestore.collection("usernames").document(uid);
                                                        documentReference3.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                timer.cancel();
                                                                Toast.makeText(EmailVerificationActivity.this, "Hesabınız başarıyla silinmiştir", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(EmailVerificationActivity.this,Authentication.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                deleteAccount.setClickable(true);
                                                Toast.makeText(EmailVerificationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                deleteAccount.setClickable(true);
                                System.out.println(e.getLocalizedMessage());
                            }
                        });
                    }
                });
            }
        });

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getUserState();
            }
        },0,2000);
    }

    private void getUserState() {
        firebaseUser.reload();
        if (firebaseUser.isEmailVerified()) {
            HashMap<String,Object> update = new HashMap<>();
            update.put("isVerified",true);
            firebaseFirestore.collection("users").document(firebaseUser.getUid()).update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    timer.cancel();
                    Intent intent = new Intent(EmailVerificationActivity.this,MainPage.class);
                    startActivity(intent);
                    finish();
                }
            });

        }
    }
}
