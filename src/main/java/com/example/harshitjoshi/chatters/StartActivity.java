package com.example.harshitjoshi.chatters;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class StartActivity extends AppCompatActivity {
    private Button regButton;
    private TextView welcome;
    private TextView logoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        welcome=findViewById(R.id.welcome);
        regButton = findViewById(R.id.start_reg_button);
        logoView=findViewById(R.id.logoText);
        YoYo.with(Techniques.Tada).duration(1000).repeat(3).playOn(logoView);
        welcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Tada).duration(1000).repeat(1).playOn(welcome);
            }
        });
    }

    public void register(View view) {
        Intent registerActivity = new Intent(StartActivity.this, RegisterActivity.class);
        startActivity(registerActivity);
    }

    public void login(View view) {
        Intent registerActivity = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(registerActivity);
    }
}
