package arsi.dev.chatout.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.EntryCommentActivity;

import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.CommentCard;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.PostHolder>{

    private ArrayList<CommentCard> commentCards;
    private Context context;
    private ViewGroup parent;

    public CommentsRecyclerAdapter(ArrayList<CommentCard> commentCards, Context context) {
        this.commentCards = commentCards;
        this.context = context;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_comment,parent,false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        CommentCard card = commentCards.get(position);

        holder.commentText.setText(card.getCommentMessage());
        holder.titleText.setText(card.getCommentTitle());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentId = card.getCommentId();
                HashMap<String,Object> writtenComments = card.getWrittenComments();
                String entryId = writtenComments.get(commentId).toString();

                Intent intent = new Intent(context, EntryCommentActivity.class);
                intent.putExtra("entryId",entryId);
                intent.putExtra("commentId",commentId);
                parent.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentCards.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder {

        TextView titleText,commentText;
        RelativeLayout card;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            titleText = itemView.findViewById(R.id.comment_card_title);
            commentText = itemView.findViewById(R.id.comment_card_comment);
            card = itemView.findViewById(R.id.comment_card);
        }
    }

    public void setType(ArrayList<CommentCard> list) {
        this.commentCards = list;
        notifyDataSetChanged();
    }
}
