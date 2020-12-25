package arsi.dev.chatout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class EmailChangeActivity extends AppCompatActivity {

    private EditText oldEmail,newEmail;
    private Button save;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_change);

        oldEmail = findViewById(R.id.email_change_old_email);
        newEmail = findViewById(R.id.email_change_new_email);
        save = findViewById(R.id.email_change_save);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String oldEmailText = oldEmail.getText().toString();
                final String newEmailText = newEmail.getText().toString();

                String uid = firebaseUser.getUid();

                final DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final String email = (String) documentSnapshot.getString("email");
                        if (oldEmailText.isEmpty() || newEmailText.isEmpty()) {
                            Toast.makeText(EmailChangeActivity.this, "Lütfen gereken alanları doldurunuz.", Toast.LENGTH_SHORT).show();
                        } else {
                            if (oldEmailText.equals(email)) {
                                if (oldEmailText.equals(newEmailText)) {
                                    Toast.makeText(EmailChangeActivity.this, "Eski E-Mailiniz ile yeni E-Mailiniz aynı olamaz.", Toast.LENGTH_SHORT).show();
                                } else {
                                    DocumentReference documentReference1 = firebaseFirestore.collection("Passwords").document(uid);

                                    documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String password = (String) documentSnapshot.getString("password");
                                            AuthCredential authCredential = EmailAuthProvider.getCredential(email,password);

                                            firebaseUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    firebaseUser.updateEmail(newEmailText).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            HashMap<String,Object> update = new HashMap<>();
                                                            update.put("email",newEmailText);

                                                            documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(EmailChangeActivity.this, "E-Mailiniz başarıyla değiştirilmiştir lütfen yeni E-Mailiniz ile tekrar giriş yapınız.", Toast.LENGTH_SHORT).show();
                                                                    firebaseAuth.signOut();
                                                                    Intent intent = new Intent(EmailChangeActivity.this,Authentication.class);
                                                                    startActivity(intent);
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
                            } else {
                                Toast.makeText(EmailChangeActivity.this, "Lütfen mevcut E-Mailinizi doğru giriniz.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }
}
