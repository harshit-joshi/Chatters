package com.example.harshitjoshi.chatters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        String current_user_id=mAuth.getCurrentUser().getUid();
        Messages messages = mMessageList.get(position);
        String from_user=messages.getFrom();
        if(from_user.equals(current_user_id))
        {
            holder.messageText.setBackgroundColor(Color.BLACK);
            holder.messageText.setTextColor(Color.YELLOW);

        }
        else
        {
            holder.messageText.setBackgroundResource(R.drawable.messege_text_background);
            holder.messageText.setTextColor(Color.BLACK);
        }
        holder.messageText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.single_user_image);

        }
    }
}