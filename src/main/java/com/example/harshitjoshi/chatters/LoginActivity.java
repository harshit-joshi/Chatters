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
import com.google.firebase.auth.FirebaseUser;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

public class LoginActivity extends AppCompatActivity {
    private EditText id;
    private EditText pass;
    private android.support.v7.widget.Toolbar loginToolbar;
    private ProgressDialog loginProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginToolbar = findViewById(R.id.login_toolbar);
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle("Login Yourself");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loginProgress = new ProgressDialog(this);
        id = findViewById(R.id.loginEmail);
        pass = findViewById(R.id.loginPassword);

        Button button = findViewById(R.id.loginSubmitButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = id.getText().toString();
                String password = pass.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    loginProgress.setTitle("Logging-in");
                    loginProgress.setMessage("Checking your crediantials");
                    loginProgress.setCanceledOnTouchOutside(false);
                    loginProgress.show();
                    LoginUser(email, password);

                } else {
                    // If sign in fails, display a message to the user.
                    loginProgress.hide();
                    Log.w("Not Success", "createUserWithEmail:failure");
                    StyleableToast.makeText(LoginActivity.this, "Enter Email and Password",
                            Toast.LENGTH_SHORT,R.style.myToast).show();
                }

            }
        });
    }

    private void LoginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if  (task.isSuccessful()) {
                            loginProgress.dismiss();
                            Log.d("success Login", "signInWithEmail:success");
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            FirebaseUser user = mAuth.getCurrentUser();

                        }
                        else
                        {
                            loginProgress.dismiss();
                            String error=" ";
                            try
                            {
                                throw task.getException();
                            }
                            catch (Exception e)
                            {
                                Log.w("Not Success", e);
                                error="Please Check Your Email and Password" ;
                            }
                            StyleableToast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG,R.style.myToast).show();
                        }
                    }

                });
    }
}

