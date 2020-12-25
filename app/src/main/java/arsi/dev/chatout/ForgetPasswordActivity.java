package arsi.dev.chatout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText email;
    private Button send;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email = findViewById(R.id.forget_password_email);
        send = findViewById(R.id.forget_password_send);
        firebaseAuth = FirebaseAuth.getInstance();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!email.getText().toString().isEmpty()) {
                    firebaseAuth.sendPasswordResetEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ForgetPasswordActivity.this, "Şifre sıfırlama e-postanız gönderilmiştir.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgetPasswordActivity.this, Authentication.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ForgetPasswordActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ForgetPasswordActivity.this, "Lütfen e-posta adresinizi giriniz.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
