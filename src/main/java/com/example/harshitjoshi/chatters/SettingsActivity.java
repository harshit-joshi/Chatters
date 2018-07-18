package com.example.harshitjoshi.chatters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private static final int GALLARY_PICK = 1;
    private DatabaseReference mUserdatabase;
    private FirebaseUser mCurrentUser;
    //Android Layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusButton;
    private Button mImageButton;
    //Firebase Storage
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mStatusButton = findViewById(R.id.changeStatus);
        mImageButton = findViewById(R.id.changeProfile);
        mDisplayImage = findViewById(R.id.circleImage);
        mName = findViewById(R.id.settingsNameText);
        mStatus = findViewById(R.id.settingsStatusText);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserdatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);
        mUserdatabase.keepSynced(true);
        mUserdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Toast.makeText(SettingsActivity.this,dataSnapshot.toString(),Toast.LENGTH_LONG).show();
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumn_nail = dataSnapshot.child("thumb_nail").getValue().toString();
                mName.setText(name);
                mStatus.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(mDisplayImage);

                        }
                    });
                } else {
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(mDisplayImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusChangeIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                startActivity(statusChangeIntent);
            }
        });
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLARY_PICK);
                /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this); */
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                final File thumb_file = new File(resultUri.getPath());
                String current_user_id = mCurrentUser.getUid();
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference file_path = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpeg");


                file_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(SettingsActivity.this,"Task is Succesfull",Toast.LENGTH_LONG).show();
                            final String download_url = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumb_task) {
                                    if (thumb_task.isSuccessful()) {
                                        String thumb_download = thumb_task.getResult().getDownloadUrl().toString();
                                        Map updateHashmap = new HashMap<>();
                                        updateHashmap.put("image", download_url);
                                        updateHashmap.put("thumb_nail", thumb_download);
                                        mUserdatabase.updateChildren(updateHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (thumb_task.isSuccessful()) {
                                                    StyleableToast.makeText(SettingsActivity.this, " Profile picture updated", Toast.LENGTH_LONG,R.style.myToast).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Not Succesfull in loading ThumbNail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(SettingsActivity.this, "Not Succesfull", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
