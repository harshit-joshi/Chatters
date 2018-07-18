package com.example.harshitjoshi.chatters;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private Toolbar mChatToolbar;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private DatabaseReference mRootRef;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private RecyclerView mMessageList;
    private SwipeRefreshLayout mRefreshLayout;

    private ImageButton mAddButton;
    private ImageButton mSendButton;
    private EditText mChatMessageView;
    private final List<Messages> messagesList =new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter messageAdapter;

    private static final int TOTAL_ITEM_TO_LOAD=10;
    private int mCurrentPageNo=1;
    private int ItemPosition=0;
    private String lastKey="";
    private String prevKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatToolbar=findViewById(R.id.chatBarLayout);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mAddButton=findViewById(R.id.chat_add_button);
        mSendButton=findViewById(R.id.chat_send_button);
        mChatMessageView=findViewById(R.id.chat_message_view);

        mRootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();
        mChatUser=getIntent().getStringExtra("user_id");

        String userName=getIntent().getStringExtra("user_name");
        LayoutInflater inflater= (LayoutInflater) this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        messageAdapter=new MessageAdapter(messagesList);
        mMessageList=findViewById(R.id.mesaage_list);
        mRefreshLayout=findViewById(R.id.message_swipe_layout);
        mLinearLayout=new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(messageAdapter);
        loadMessages();

        //Custom Bar  Items
        mTitleView=findViewById(R.id.chat_custom_bar_user_name);
        mLastSeenView=findViewById(R.id.chat_custom_bar_last_seen);
        mProfileImage=findViewById(R.id.custom_bar_image);
        mTitleView.setText(userName);

        mRootRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();
                if(online.equals("true"))
                {
                    mLastSeenView.setText("Online");
                }
                else
                {
                    GetTimeAgo getTimeAgo=new GetTimeAgo();
                    long lastTime=Long.parseLong(online);
                    String lastSeenTime=GetTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser))
                {
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUserId+"/" +mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+ "/"+mCurrentUserId,chatAddMap);
                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {
                            if(databaseError !=null)
                            {
                                Log.e("Chat_log",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }



            private void sendMessage() {
                String message=mChatMessageView.getText().toString();
                if(!TextUtils.isEmpty(message))
                {
                    String current_user_ref="messages/" +mCurrentUserId +"/" +mChatUser;
                    String chat_user_ref="messages/"+mChatUser+"/"+mCurrentUserId;
                    DatabaseReference user_message_push=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
                    String push_id=user_message_push.getKey();
                    Map messageMap=new HashMap();
                    messageMap.put("message",message  );
                    messageMap.put("seen",false);
                    messageMap.put("type","text");
                    messageMap.put("time",ServerValue.TIMESTAMP);
                    messageMap.put("from",mCurrentUserId);
                    Map messageUserMap=new HashMap();
                    messageUserMap.put(current_user_ref+"/" +push_id,messageMap);
                    messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);
                    mChatMessageView.setText("");

                    mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
                    mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
                    mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
                    mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);
                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {
                            if(databaseError !=null)
                            {
                                Log.e("Chat_log",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPageNo++;
                ItemPosition=0;
                //Adding this line is compulsory otherwise it will load message only once
                loadMoreMessages();
               // mRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadMoreMessages()
    {
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery=messageRef.orderByKey().endAt(lastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message=dataSnapshot.getValue(Messages.class);
                messagesList.add(ItemPosition,message);
                String messageKey=dataSnapshot.getKey();
               // ItemPosition++;
                if (!prevKey.equals(messageKey))
                {
                    messagesList.add(ItemPosition++ ,message);
                }
                else
                {
                    prevKey=lastKey;
                }
                if(ItemPosition==1)
                {
                    lastKey=messageKey;


                }
                messageAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                //For Sending user to End of Recyler View So user can se last message
                mLinearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages()
    {
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        //        //This is for how much message we need in a single list View
        Query messageQuery=messageRef.limitToLast(mCurrentPageNo*TOTAL_ITEM_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message=dataSnapshot.getValue(Messages.class);

                ItemPosition++;
                if(ItemPosition==1)
                {   //Key of the current Item at the top of the list otherwise it will repeat it again
                    String messageKey=dataSnapshot.getKey();
                    lastKey=messageKey;
                    prevKey=messageKey;

                }
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();

                //For Sending user to End of Rececyler View So user can se last message
                mMessageList.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);
               // mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
