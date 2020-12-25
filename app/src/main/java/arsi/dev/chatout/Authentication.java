package arsi.dev.chatout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class Authentication extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private EditText eMailText, passwordText;
    private TextView forgetPassword, textView, textView1, textView2;
    private GifImageView gifImageView;
    private Button button;
    private ImageView imageView, imageView2, imageView3, imageView4;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        imageView3 = findViewById(R.id.imageView10);
        imageView4 = findViewById(R.id.imageView11);
        textView1 = findViewById(R.id.textView4);
        constraintLayout = findViewById(R.id.background);
        textView2 = findViewById(R.id.textView9);
        gifImageView = findViewById(R.id.spinner);
        button = findViewById(R.id.login_button);
        imageView2 = findViewById(R.id.imageView3);
        imageView = findViewById(R.id.profilePicture);
        eMailText = findViewById(R.id.eMailText);
        passwordText = findViewById(R.id.passwordText);
        forgetPassword = findViewById(R.id.textView);
        textView = findViewById(R.id.textView2);

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Authentication.this, ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });

        Intent start = getIntent();
        Bundle bundle = start.getExtras();

        if (firebaseAuth.getCurrentUser() != null) {
            invisible();
            constraintLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            gifImageView.setVisibility(View.VISIBLE);
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String token = instanceIdResult.getToken();
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getUid());
                    Map<String, Object> update = new HashMap<>();
                    update.put("pushToken", token);
                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                Intent intent = new Intent(Authentication.this, MainPage.class);
                                if (bundle != null) {
                                    intent.putExtras(bundle);
                                }

                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(Authentication.this, EmailVerificationActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            });
        }
    }

    public void invisible() {
        imageView3.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        eMailText.setVisibility(View.INVISIBLE);
        passwordText.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        forgetPassword.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);
        textView1.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
    }

    public void login(View view) {
        button.setClickable(false);
        String email = eMailText.getText().toString();
        String password = passwordText.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            String token = instanceIdResult.getToken();
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getUid());
                            Map<String, Object> update = new HashMap<>();
                            update.put("pushToken", token);
                            update.put("email",email);
                            documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (authResult.getUser().isEmailVerified()) {
                                        DocumentReference documentReference1 = firebaseFirestore.collection("Passwords").document(authResult.getUser().getUid());
                                        HashMap<String, Object> passwordUpdate = new HashMap<>();
                                        passwordUpdate.put("password", password);
                                        documentReference1.set(passwordUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(Authentication.this, MainPage.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    } else {
                                        Intent intent = new Intent(Authentication.this, EmailVerificationActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    button.setClickable(true);
                    Toast.makeText(Authentication.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            button.setClickable(true);
            Toast.makeText(Authentication.this, "Lütfen gerekli alanları doldurunuz.", Toast.LENGTH_LONG).show();
        }
    }

    public void goSignUp(View view) {
        Intent intent = new Intent(Authentication.this, SignUp.class);
        startActivity(intent);
    }

}

