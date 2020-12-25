package arsi.dev.chatout.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import arsi.dev.chatout.OthersProfileActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.FollowersCard;

public class FollowersRecyclerAdapter extends RecyclerView.Adapter<FollowersRecyclerAdapter.PostHolder> {

    private Context context;
    private ArrayList<FollowersCard> followersCards;
    private ViewGroup parent;

    public FollowersRecyclerAdapter(Context context, ArrayList<FollowersCard> followersCards) {
        this.context = context;
        this.followersCards = followersCards;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_followers, parent, false);

        return new FollowersRecyclerAdapter.PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowersRecyclerAdapter.PostHolder holder, int position) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FollowersCard card = followersCards.get(position);
        holder.username.setText(card.getUsername());
        Picasso.get().load(card.getPhotoUri()).into(holder.followersPp);
        holder.block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = card.getUsername() + " kullanıcısını engellemek istiyor musunuz?";
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Dikkat");
                builder.setMessage(messageText);
                builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HashMap<String, Object> myUpdate = new HashMap<>();
                        myUpdate.put("followers", FieldValue.arrayRemove(card.getFollowersId()));
                        myUpdate.put("followings", FieldValue.arrayRemove(card.getFollowersId()));
                        myUpdate.put("blockedArray", FieldValue.arrayUnion(card.getFollowersId()));
                        myUpdate.put("notificationOpenedUids", FieldValue.arrayRemove(card.getFollowersId()));
                        myUpdate.put("notificationOpenedByUids", FieldValue.arrayRemove(card.getFollowersId()));

                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(myUid);
                        documentReference.set(myUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HashMap<String, Object> userUpdate = new HashMap<>();
                                userUpdate.put("followers", FieldValue.arrayRemove(myUid));
                                userUpdate.put("followings", FieldValue.arrayRemove(myUid));
                                userUpdate.put("blockedByArray", FieldValue.arrayUnion(myUid));
                                userUpdate.put("notificationOpenedUids", FieldValue.arrayRemove(myUid));
                                userUpdate.put("notificationOpenedByUids", FieldValue.arrayRemove(myUid));

                                DocumentReference documentReference1 = FirebaseFirestore.getInstance().collection("users").document(card.getFollowersId());
                                documentReference1.set(userUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Kullanıcı engellendi", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
                builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });
        holder.followersCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("searchIdUsername", card.getFollowersId());
                parent.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return followersCards.size();
    }

    public void filterList(ArrayList<FollowersCard> filteredList) {
        followersCards = filteredList;
        notifyDataSetChanged();
    }

    class PostHolder extends RecyclerView.ViewHolder {

        View followersCard;
        TextView username;
        ImageView followersPp, block;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            block = itemView.findViewById(R.id.followers_card_block);
            username = itemView.findViewById(R.id.followers_card_username);
            followersCard = itemView.findViewById(R.id.followersCard);
            followersPp = itemView.findViewById(R.id.profilePictureFollowers);

        }
    }
}
