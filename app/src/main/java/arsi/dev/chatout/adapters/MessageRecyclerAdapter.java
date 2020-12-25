package arsi.dev.chatout.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import arsi.dev.chatout.MessageActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.MessageCard;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.PostHolder> {

    private final int MSG_TYPE_LEFT = 0;
    private final int MSG_TYPE_RIGHT = 1;
    private ArrayList<MessageCard> messageCards;

    public MessageRecyclerAdapter(ArrayList<MessageCard> messageCards, MessageActivity messageActivity) {
        this.messageCards = messageCards;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.chat_item_left, parent, false);
            return new PostHolder(view);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.chat_item_right, parent, false);
            return new PostHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        MessageCard card = messageCards.get(position);

        Date date = card.getTime().toDate();
        String time = "";
        if (date.getHours() < 10) {
            if (date.getMinutes() < 10) {
                time = "0" + date.getHours() + ":" + "0" + date.getMinutes();
            } else {
                time = "0" + date.getHours() + ":" + date.getMinutes();
            }
        } else {
            if (date.getMinutes() < 10) {
                time = date.getHours() + ":" + "0" + date.getMinutes();
            } else {
                time = date.getHours() + ":" + date.getMinutes();
            }
        }

        if (holder.getItemViewType() == MSG_TYPE_RIGHT)
            holder.username.setTextColor(Color.parseColor("#000000"));
        else holder.username.setTextColor(Color.parseColor(card.getUsernameColor()));

        holder.message.setText(card.getMessage());
        holder.username.setText(card.getUsername());
        holder.time.setText(time);
    }

    @Override
    public int getItemCount() {
        return messageCards.size();
    }

    @Override
    public int getItemViewType(int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (messageCards.get(position).getSenderUid().equals(uid)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class PostHolder extends RecyclerView.ViewHolder {
        TextView message, username, time;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_item_message);
            username = itemView.findViewById(R.id.chat_item_username);
            time = itemView.findViewById(R.id.chat_item_send_time);
        }
    }
}
