package com.example.harshitjoshi.chatters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        mToolbar = findViewById(R.id.all_users_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsersDatbase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatbase.keepSynced(true);
        mUsersList = findViewById(R.id.usersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class, R.layout.single_user_layout_file, UsersViewHolder.class, mUsersDatbase) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setDisplayName(model.getName());
                viewHolder.setUserStatus(model.getStatus());
                viewHolder.setUserImage(model.getImage());
                final String user_id=getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent=new Intent(AllUsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });

            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDisplayName(String name) {

            TextView userNameView = mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);

        }

        public void setUserStatus(String status) {

            TextView userStatusView = mView.findViewById(R.id.single_user_status);
            userStatusView.setText(status);


        }

        public void setUserImage(String image) {

            CircleImageView userImageView = mView.findViewById(R.id.single_user_image);

            Picasso.get().load(image).placeholder(R.drawable.profile).into(userImageView);

        }


    }
}