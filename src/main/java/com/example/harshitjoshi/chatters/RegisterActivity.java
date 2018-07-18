package com.example.harshitjoshi.chatters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText fullName;
    private EditText id;
    private EditText pass;
    private Button createAccountButton;
    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar toolbar;
    private ProgressDialog regProgress;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        regProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        fullName = findViewById(R.id.registerName);
        id = findViewById(R.id.registerEmail);
        pass = findViewById(R.id.registerPassword);
        createAccountButton = findViewById(R.id.registerCreateAccountButton);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    String name = fullName.getText().toString();
                    String email = id.getText().toString();
                    String password = pass.getText().toString();
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                        regProgress.setTitle("Registering User");
                        regProgress.setMessage("Registering your Details");
                        regProgress.setCanceledOnTouchOutside(false);
                        regProgress.show();
                        registerUser(name, email, password);

                    }
                    else
                    {
                        StyleableToast.makeText(getApplicationContext(),"Enter All Details",Toast.LENGTH_LONG,R.style.myToast).show();
                    }

                }
            }
        });
    }


    private void registerUser(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("status", "Hey this is default status ");
                            userMap.put("image", "default");
                            userMap.put("thumb_nail", "default");
                            databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("successful", "createUserWithEmail:success");
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    finish();
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                           /* Log.d("Not Success", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show(); */
                            regProgress.dismiss();
                            String error = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                error = "Weak Password ";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                error = "Wrong Email";
                            } catch (FirebaseAuthUserCollisionException e) {
                                error = "Account Already Exists!";
                            } catch (Exception e) {
                                Log.w("Not Success", e);
                                error = "There is something Wrong,Please Enter your details again" ;
                            }
                            StyleableToast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG,R.style.myToast).show();
                        }

                    }
                });
    }
}
