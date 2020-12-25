package arsi.dev.chatout.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.EntryCommentActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.InsideEntryCard;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InsideEntryRecyclerAdapter extends RecyclerView.Adapter<InsideEntryRecyclerAdapter.PostHolder> {

    private ArrayList<InsideEntryCard> cards;
    private Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String entryId;
    private ViewGroup parent;

    public InsideEntryRecyclerAdapter(ArrayList<InsideEntryCard> cards, Context context, String entryId) {
        this.cards = cards;
        this.context = context;
        this.entryId = entryId;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_entry_inside,parent,false);
        return new InsideEntryRecyclerAdapter.PostHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        InsideEntryCard card = cards.get(position);

        Date date = card.getTimestamp().toDate();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String dateText = localDate.getDayOfMonth() + "." + localDate.getMonthValue() + "." + localDate.getYear();

        holder.time.setText(dateText);
        holder.comment.setText(card.getComment());
        holder.likes.setText(String.valueOf(card.getLikes().size()));
        holder.dislikes.setText(String.valueOf(card.getDislikes().size()));
        holder.username.setText(card.getUsername());

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EntryCommentActivity.class);
                intent.putExtra("commentId",card.getcommentId());
                intent.putExtra("entryId",entryId);
                parent.getContext().startActivity(intent);
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = firebaseAuth.getCurrentUser().getUid();
               if(!card.getLikes().contains(uid)) {
                   Map<String,Object> data = new HashMap<>();
                   data.put("likesArray", FieldValue.arrayUnion(uid));
                   data.put("dislikesArray",FieldValue.arrayRemove(uid));
                   DocumentReference documentReference = firebaseFirestore.collection("entries").document(entryId).collection("comments").document(card.getcommentId());
                   documentReference.set(data, SetOptions.merge());
               } else {
                   Map<String,Object> data = new HashMap<>();
                   data.put("likesArray",FieldValue.arrayRemove(uid));
                   DocumentReference documentReference = firebaseFirestore.collection("entries").document(entryId).collection("comments").document(card.getcommentId());
                   documentReference.set(data, SetOptions.merge());
               }
            }
        });

        holder.dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                if(!card.getDislikes().contains(uid)){
                    Map<String,Object> data = new HashMap<>();
                    data.put("dislikesArray",FieldValue.arrayUnion(uid));
                    data.put("likesArray",FieldValue.arrayRemove(uid));
                    DocumentReference documentReference = firebaseFirestore.collection("entries").document(entryId).collection("comments").document(card.getcommentId());
                    documentReference.set(data, SetOptions.merge());
                } else {
                    Map<String,Object> data = new HashMap<>();
                    data.put("dislikesArray",FieldValue.arrayRemove(uid));
                    DocumentReference documentReference = firebaseFirestore.collection("entries").document(entryId).collection("comments").document(card.getcommentId());
                    documentReference.set(data, SetOptions.merge());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        TextView username, likes, dislikes, time, comment;
        View insideEntryCard;
        ImageView like, dislike;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            insideEntryCard = itemView.findViewById(R.id.insideEntryView);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            dislikes = itemView.findViewById(R.id.dislikes);
            time = itemView.findViewById(R.id.date);
            comment = itemView.findViewById(R.id.comment);
            like = itemView.findViewById(R.id.imageView2);
            dislike = itemView.findViewById(R.id.imageView5);
        }
    }
}
