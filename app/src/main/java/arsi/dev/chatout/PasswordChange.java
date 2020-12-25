package arsi.dev.chatout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class PasswordChange extends AppCompatActivity {

    private EditText oldPassword,newPassword,newPasswordAgain;
    private Button save;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        oldPassword = findViewById(R.id.password_change_old_password);
        newPassword = findViewById(R.id.password_change_new_password);
        newPasswordAgain = findViewById(R.id.password_change_new_password_again);
        save = findViewById(R.id.password_change_save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String oldPasswordText = oldPassword.getText().toString();
                final String newPasswordText = newPassword.getText().toString();
                final String newPasswordAgainText = newPasswordAgain.getText().toString();

                String uid = firebaseAuth.getCurrentUser().getUid();
                final DocumentReference documentReference = firebaseFirestore.collection("Passwords").document(uid);

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String password = (String) documentSnapshot.getString("password");

                        if (password.equals(oldPasswordText)) {
                            if (newPasswordText.equals(password)) {
                                Toast.makeText(PasswordChange.this, "Yeni şifreniz ile eski şifreniz aynı olamaz.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (newPasswordText.equals(newPasswordAgainText)) {
                                    String email = firebaseAuth.getCurrentUser().getEmail();
                                    AuthCredential credential = EmailAuthProvider.getCredential(email,password);

                                    firebaseUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseAuth.getCurrentUser().updatePassword(newPasswordText).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    HashMap<String,Object> update = new HashMap<>();
                                                    update.put("password",newPasswordText);

                                                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(PasswordChange.this, "Şifreniz başarıyla değiştirilmiştir.", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(PasswordChange.this,MainPage.class);
                                                            finish();
                                                            startActivity(intent);
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    if (e.getClass().toString().equals("class com.google.firebase.auth.FirebaseAuthWeakPasswordException")) {
                                                        Toast.makeText(PasswordChange.this, "Şifreniz en az 6 karakterli olmalıdır.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(PasswordChange.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    Toast.makeText(PasswordChange.this, "Girdiğiniz yeni şifreler birbirinden farklı lütfen kontrol ediniz.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(PasswordChange.this, "Lütfen önceki şifrenizi doğru giriniz.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
