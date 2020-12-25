package arsi.dev.chatout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arsi.dev.chatout.models.SignUpModel;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private EditText userNameText, eMailText, passwordText, passwordAgainText;
    private Uri imageData;
    private Bitmap selectedImage;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ImageView imageView;
    private SignUpModel signUpModel;
    private ImageView signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        userNameText = findViewById(R.id.userNameText);
        eMailText = findViewById(R.id.eMailText);
        passwordText = findViewById(R.id.passwordText);
        passwordAgainText = findViewById(R.id.passwordAgainText);
        imageView = findViewById(R.id.profilePicture);
        signUp = findViewById(R.id.sign_up_button);

        signUpModel = new SignUpModel();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp.setClickable(false);
                final String email = eMailText.getText().toString();
                String password = passwordText.getText().toString();
                String passwordAgain = passwordAgainText.getText().toString();
                String userName = userNameText.getText().toString();
                if (email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty() ||
                        userName.isEmpty() || imageData == null) {
                    Toast.makeText(SignUp.this, "Lütfen gerekli tüm alanları doldurunuz.", Toast.LENGTH_SHORT).show();
                    signUp.setClickable(true);
                } else {
                    if (password.equals(passwordAgain)) {
                        if (password.length() < 6) {
                            signUp.setClickable(true);
                            Toast.makeText(SignUp.this, "Parolanız 6 karakterden kısa olamaz.", Toast.LENGTH_SHORT).show();
                        }
                        else if (userName.length() < 6) {
                            signUp.setClickable(true);
                            Toast.makeText(SignUp.this, "Kullanıcı adınız 6 karakterden kısa olamaz.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (signUpModel.getUsernames().contains(userName)) {
                                signUp.setClickable(true);
                                Toast.makeText(SignUp.this, "Bu kullanıcı adı alınmıştır lütfen başka bir kullanıcı adı deneyiniz.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        final String userUid = firebaseAuth.getCurrentUser().getUid();
                                        final String imageName = userUid + "/ProfilePicture.jpg";
                                        storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                StorageReference storageReference1 = FirebaseStorage.getInstance().getReference(imageName);
                                                storageReference1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String photoUri = uri.toString();
                                                        ArrayList<String> activeChats = new ArrayList<>();
                                                        ArrayList<String> moderatorChats = new ArrayList<>();
                                                        ArrayList<String> lastSearchs = new ArrayList<>();
                                                        ArrayList<String> followers = new ArrayList<>();
                                                        ArrayList<String> followings = new ArrayList<>();
                                                        ArrayList<String> blockedArray = new ArrayList<>();
                                                        ArrayList<String> blockedByArray = new ArrayList<>();
                                                        ArrayList<String> notificationOpenedUids = new ArrayList<>();
                                                        ArrayList<String> notificationOpenedByUids = new ArrayList<>();
                                                        ArrayList<String> openedEntries = new ArrayList<>();
                                                        HashMap<String, Object> writtenComments = new HashMap<>();

                                                        final Map<String, Object> user = new HashMap<>();
                                                        user.put("username", userName);
                                                        user.put("email", email);
                                                        user.put("photoUri", photoUri);
                                                        user.put("aboutMe", "");
                                                        user.put("activeChats", activeChats);
                                                        user.put("moderatorChats", moderatorChats);
                                                        user.put("isPremium", false);
                                                        user.put("userId", userUid);
                                                        user.put("lastSearchs", lastSearchs);
                                                        user.put("followers", followers);
                                                        user.put("followings", followings);
                                                        user.put("blockedArray", blockedArray);
                                                        user.put("blockedByArray", blockedByArray);
                                                        user.put("notificationOpenedUids", notificationOpenedUids);
                                                        user.put("notificationOpenedByUids", notificationOpenedByUids);
                                                        user.put("activeChat", "");
                                                        user.put("openedEntries", openedEntries);
                                                        user.put("writtenComments", writtenComments);
                                                        user.put("isVerified", false);

                                                        firebaseFirestore.collection("users").document(userUid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                HashMap<String, Object> passwordData = new HashMap<>();
                                                                passwordData.put("password", password);
                                                                firebaseFirestore.collection("Passwords").document(userUid).set(passwordData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Intent intent = new Intent(SignUp.this, EmailVerificationActivity.class);
                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                signUp.setClickable(true);
                                                                                Toast.makeText(SignUp.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                signUp.setClickable(true);
                                                                Toast.makeText(SignUp.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        signUp.setClickable(true);
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                signUp.setClickable(true);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        signUp.setClickable(true);
                                        Toast.makeText(SignUp.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    } else {
                        signUp.setClickable(true);
                        Toast.makeText(SignUp.this, "Passwords doesnt match", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        getUsernames();
    }

    private void getUsernames() {
        CollectionReference collectionReference = firebaseFirestore.collection("usernames");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        HashMap<String, Object> data = (HashMap<String, Object>) snapshot.getData();
                        String username = (String) data.get("username");
                        signUpModel.getUsernames().add(username);
                    }
                }
            }
        });
    }

    public void selectImage(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            CropImage.activity()
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setFixAspectRatio(true)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setFixAspectRatio(true)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageData = result.getUri();
                try {
                    if (Build.VERSION.SDK_INT >= 28) {
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageData);
                        selectedImage = ImageDecoder.decodeBitmap(source);
                        imageView.setImageBitmap(selectedImage);
                    } else {
                        selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                        imageView.setImageBitmap(selectedImage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
