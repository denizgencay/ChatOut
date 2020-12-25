package arsi.dev.chatout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class Settings extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private boolean isPremium;
    private ImageView changePassword, changeEmail, premium, signOut, blockedUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        isPremium = intent.getBooleanExtra("isPremium", false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        setResult(1);

        changePassword = findViewById(R.id.settings_change_password);
        changeEmail = findViewById(R.id.settings_change_email);
        premium = findViewById(R.id.settings_premium);
        signOut = findViewById(R.id.settings_signout);
        blockedUsers = findViewById(R.id.settings_blocked_users);

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, PasswordChange.class);
                startActivity(intent);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, EmailChangeActivity.class);
                startActivity(intent);
            }
        });

        blockedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, BlockedUsersActivity.class);
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                HashMap<String, Object> update = new HashMap<>();
                update.put("pushToken", FieldValue.delete());
                DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseAuth.signOut();
                        setResult(0);
                        finish();
                    }
                });
            }
        });

        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPremium) {
                    Intent intent = new Intent(Settings.this, PremiumActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(Settings.this, "Zaten satın almış olduğunuz bir premium üyeliğiniz bulunmakta.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
