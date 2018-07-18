package com.example.harshitjoshi.chatters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.tapadoo.alerter.Alerter;

public class StatusActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar mToolbar;
    private TextInputEditText mStatus;
    private Button mSaveButton;
    //FireBase Code
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);

        mToolbar = findViewById(R.id.statusActivityBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = findViewById(R.id.status_input);
        mSaveButton = findViewById(R.id.statusSaveChangeButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ProgressBar
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Updating Status");
                mProgress.show();
                String status = mStatus.getText().toString();                    // getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                            Alerter.create(StatusActivity.this)
                                    .setTitle("Congraulations !")
                                    .setText("Status Updated Click on this screen to go to your Profile")
                                    .setIcon(R.drawable.smiley_white)
                                    .setBackgroundColorRes(R.color.lightBlue)
                                    .setDuration(5000)
                                    .enableSwipeToDismiss()
                                    .enableProgress(true)
                                    .setProgressColorRes(R.color.black)
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent profileIntent=new Intent(StatusActivity.this,ProfileActivity.class);
                                            startActivity(profileIntent);
                                        }
                                    })
                                    .show();
                            //StyleableToast.makeText(getApplicationContext(),"Congratulations ! Status Updated",Toast.LENGTH_LONG,R.style.myToast).show();
                        } else {
                            StyleableToast.makeText(getApplicationContext(), "OOPS!!! There was some error while updating", Toast.LENGTH_LONG,R.style.myToast).show();
                        }
                    }
                });

            }
        });


    }
}
