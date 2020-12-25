package arsi.dev.chatout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileEdit extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private EditText aboutMe;
    private Uri imageData;
    private Bitmap selectedImage;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ImageView profilePicture;
    private TextView usernameText;
    private RelativeLayout changeProfilePhoto;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        changeProfilePhoto = findViewById(R.id.profile_edit_profile_photo_view);
        profilePicture = findViewById(R.id.profile_edit_profile_photo);
        usernameText = findViewById(R.id.profile_edit_username);
        aboutMe = findViewById(R.id.profile_edit_about_me);
        save = findViewById(R.id.profile_edit_save_button);

        changeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ProfileEdit.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileEdit.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    CropImage.activity()
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .setFixAspectRatio(true)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(ProfileEdit.this);
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userUid = firebaseAuth.getCurrentUser().getUid();
                final String imageName = userUid + "/ProfilePicture.jpg";
                if (imageData != null) {
                    storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            StorageReference newReference = firebaseStorage.getReference(imageName);
                            newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String aboutMeText = aboutMe.getText().toString();

                                    HashMap<String, Object> update = new HashMap<>();
                                    update.put("aboutMe", aboutMeText);
                                    update.put("photoUri", uri.toString());
                                    DocumentReference documentReference = firebaseFirestore.collection("users").document(userUid);

                                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ProfileEdit.this, "Değişiklikler Kaydedildi", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileEdit.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileEdit.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    String aboutMeText = aboutMe.getText().toString();

                    HashMap<String, Object> update = new HashMap<>();
                    update.put("aboutMe", aboutMeText);
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(userUid);

                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileEdit.this, "Değişiklikler Kaydedildi", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        getData();
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
                        profilePicture.setImageBitmap(selectedImage);
                    } else {
                        selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                        profilePicture.setImageBitmap(selectedImage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getData() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Toast.makeText(ProfileEdit.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    String username = (String) data.get("username");
                    String photoUri = (String) data.get("photoUri");
                    String aboutMeText = (String) data.get("aboutMe");

                    if (!aboutMeText.isEmpty()) {
                        aboutMe.setText(aboutMeText);
                    } else {
                        aboutMe.setHint("Bize kendinizden bahsedin.");
                    }
                    usernameText.setText(username);
                    Picasso.get().load(photoUri).noFade().into(profilePicture);
                }
            }
        });
    }
}
