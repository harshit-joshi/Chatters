package com.example.harshitjoshi.chatters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private RecyclerView mFriendListRecycler;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView=inflater.inflate(R.layout.fragment_friends,container,false);
        mFriendListRecycler = mMainView.findViewById(R.id.friendsListRecycler);
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendDatabase.keepSynced(true);
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users");
        mUserDatabase.keepSynced(true);
        mFriendListRecycler.setHasFixedSize(true);
        mFriendListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        //Inflate Layout Of  Fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class, R.layout.single_user_layout_file, FriendsViewHolder.class, mFriendDatabase) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends model, int position) {

                viewHolder.setDate(model.getDate());
                final String list_user_id=getRef(position).getKey();
                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName=dataSnapshot.child("name").getValue().toString();
                        String userImage=dataSnapshot.child("image").getValue().toString();
                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }
                        viewHolder.setName(userName);
                        viewHolder.setUserImage(userImage);
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[]=new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                builder.setTitle("Select option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        if(which==0)
                                        {
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(which==1)
                                        {
                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendListRecycler.setAdapter(firebaseRecyclerAdapter);
    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setDate(String date)
        {
            TextView friendshipDayView =mView.findViewById(R.id.single_user_status);
            friendshipDayView.setText(date);
        }
        public void setName(String name)
        {
            TextView userNameView = mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);

        }
        public void setUserImage(String image) {

            CircleImageView userImageView = mView.findViewById(R.id.single_user_image);

            Picasso.get().load(image).placeholder(R.drawable.profile).into(userImageView);

        }
        public void setUserOnline(String online_status)
        {
            ImageView userOnline=mView.findViewById(R.id.onlineStatusImage);
            if(online_status.equals("true"))
            {
                userOnline.setVisibility(View.VISIBLE);
            }
            else
            {
                userOnline.setVisibility(View.INVISIBLE);
            }
        }
    }
}