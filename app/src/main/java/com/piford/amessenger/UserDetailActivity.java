package com.piford.amessenger;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.piford.amessenger.common.ImageFilePath;
import com.piford.amessenger.models.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class UserDetailActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 102;
    FirebaseAuth auth;
    FirebaseUser user;
    EditText etName, etPhoneNumber;
    Button btnProceed;
    ImageView imgPicture;
    String oldImageUrl;
    String imageURL;
    String realPath;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_user_detail);
        auth = FirebaseAuth.getInstance ( );
        user = auth.getCurrentUser ( );


        etName = findViewById (R.id.etName);
        etPhoneNumber = findViewById (R.id.etPhoneNumber);
        btnProceed = findViewById (R.id.btnProceed);
        imgPicture = findViewById (R.id.imgPicture);
        imgPicture.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick (View view) {
                Intent intent = new Intent ( );
                intent.setType ("image/*");
                intent.setAction (Intent.ACTION_GET_CONTENT);
                startActivityForResult (Intent.createChooser (intent, "Select Picture"), PICK_IMAGE);
            }
        });
        btnProceed.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick (View view) {
                uploadImageToFirebase ( );
            }
        });
        etName.setText (user.getDisplayName ( ));
        etPhoneNumber.setText (user.getPhoneNumber ( ));
        if (user.getPhotoUrl ( ) != null) {
            oldImageUrl = ImageFilePath.getPath (this, user.getPhotoUrl ( ));
        }
        Glide.with (this).load (user.getPhotoUrl ( )).into (imgPicture);


        demandPermission ( );
        if (isDefaultProfile ( )) {
            gotoHome ( );
        }
    }

    private void setDefaultProfile () {
        SharedPreferences preferences = getSharedPreferences ("appConfig", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit ( );
        edit.putBoolean ("CONFIGURED", true);
        edit.commit ( );
    }

    private boolean isDefaultProfile () {
        SharedPreferences preferences = getSharedPreferences ("appConfig", MODE_PRIVATE);
        return preferences.getBoolean ("CONFIGURED", false);
    }

    private void demandPermission () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.READ_EXTERNAL_STORAGE) != 0) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        }
    }

    private void saveProfileData () {
        String name = etName.getText ( ).toString ( );
        User objUser = new User ( );
        objUser.setName (name);
        objUser.setPhone (user.getPhoneNumber ( ));
        objUser.setProfilePic (imageURL == null ? oldImageUrl : imageURL);
        updateProfile (objUser);

    }

    private void updateProfile (final User objUser) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder ( )
                .setDisplayName (objUser.getName ( ))
                .setPhotoUri (Uri.parse (objUser.getProfilePic ( )))
                .build ( );

        user.updateProfile (profileUpdates)
                .addOnCompleteListener (new OnCompleteListener<Void> ( ) {
                    @Override
                    public void onComplete (@NonNull Task<Void> task) {
                        if (task.isSuccessful ( )) {
                            saveToDatabase (objUser);
                        } else {
                            dialog.dismiss ( );
                            Toast.makeText (UserDetailActivity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show ( );

                        }
                    }
                });
    }

    private void saveToDatabase (User objUser) {
        FirebaseDatabase database = FirebaseDatabase.getInstance ( );
        DatabaseReference userRef = database.getReference ( ).child ("users").child (user.getPhoneNumber ( ));
        Task task = userRef.setValue (objUser);

        task.addOnCompleteListener (new OnCompleteListener ( ) {
            @Override
            public void onComplete (@NonNull Task task) {
                dialog.dismiss ( );
                Toast.makeText (UserDetailActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show ( );
                setDefaultProfile ( );
                gotoHome ( );
            }
        });
        task.addOnCanceledListener (new OnCanceledListener ( ) {
            @Override
            public void onCanceled () {
                dialog.dismiss ( );
                Toast.makeText (UserDetailActivity.this, "Operation canceled", Toast.LENGTH_SHORT).show ( );
            }
        });
        task.addOnFailureListener (new OnFailureListener ( ) {
            @Override
            public void onFailure (@NonNull Exception e) {
                dialog.dismiss ( );
                Toast.makeText (UserDetailActivity.this, "Failed to save", Toast.LENGTH_SHORT).show ( );
            }
        });

    }

    private void gotoHome () {
        Intent i = new Intent (UserDetailActivity.this, UserListActivity.class);
        startActivity (i);
        finish ( );
    }


    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData ( );
            realPath = ImageFilePath.getPath (this, imageUri);
            Glide.with (this).load (imageUri).into (imgPicture);
        }
    }

    ProgressDialog dialog;

    private void uploadImageToFirebase () {
        dialog = new ProgressDialog (this);
        dialog.setCancelable (false);
        dialog.setMessage ("please wait...");
        dialog.setTitle ("Saving...");
        dialog.show ( );
        if (realPath == null) {
            saveProfileData ( );
            return;
        }
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance ( );
            StorageReference storageReference = storage.getReference ( ).child ("images");
            String randomName = new Date ( ).hashCode ( ) + ".jpg";
            FileInputStream fis = new FileInputStream (realPath);
            UploadTask task = storageReference.child (randomName).putStream (fis);
            task.addOnCompleteListener (new OnCompleteListener<UploadTask.TaskSnapshot> ( ) {
                @Override
                public void onComplete (@NonNull Task<UploadTask.TaskSnapshot> task) {
                    imageURL = task.getResult ( ).getDownloadUrl ( ).toString ( );
                    saveProfileData ( );

                }
            });
            task.addOnFailureListener (new OnFailureListener ( ) {
                @Override
                public void onFailure (@NonNull Exception e) {
                    dialog.dismiss ( );
                    Toast.makeText (UserDetailActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show ( );
                }
            });
//            fis.close ( );
        } catch (IOException e) {
            e.printStackTrace ( );
        }

    }

}
