package com.example.harshitjoshi.chatters;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.chatters);
        if(mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }
        //tabs
        mViewPager = findViewById(R.id.main_tab_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToStart();
        }
        else
        {
            mUserRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null)
        {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }


    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_button) {
            FirebaseAuth.getInstance().signOut();
            //Hey thats for changing when user sign outs
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            sendToStart();
        }
        if (item.getItemId() == R.id.main_all_users_button) {
            Intent allUsers = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(allUsers);
        }
        if (item.getItemId() == R.id.main_account_button) {
            Intent accountIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(accountIntent);

        }
        return true;
    }
}
