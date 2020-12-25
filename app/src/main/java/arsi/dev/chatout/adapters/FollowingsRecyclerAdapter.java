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

import arsi.dev.chatout.OthersProfileActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.FollowingCard;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class FollowingsRecyclerAdapter extends RecyclerView.Adapter<FollowingsRecyclerAdapter.PostHolder> {

    private ArrayList<FollowingCard> followingCards;
    private Context context;
    private ViewGroup parent;

    public FollowingsRecyclerAdapter(ArrayList<FollowingCard> followingCards, Context context) {
        this.followingCards = followingCards;
        this.context = context;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_following,parent,false);

        return new FollowingsRecyclerAdapter.PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingsRecyclerAdapter.PostHolder holder, int position) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FollowingCard card = followingCards.get(position);
        holder.username.setText(card.getUsername());
        Picasso.get().load(card.getPhotoUri()).noFade().into(holder.followingsPp);
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

                        HashMap<String,Object> myUpdate = new HashMap<>();
                        myUpdate.put("followers", FieldValue.arrayRemove(card.getFollowingId()));
                        myUpdate.put("followings",FieldValue.arrayRemove(card.getFollowingId()));
                        myUpdate.put("blockedArray",FieldValue.arrayUnion(card.getFollowingId()));
                        myUpdate.put("notificationOpenedUids",FieldValue.arrayRemove(card.getFollowingId()));
                        myUpdate.put("notificationOpenedByUids",FieldValue.arrayRemove(card.getFollowingId()));

                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(myUid);
                        documentReference.set(myUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HashMap<String,Object> userUpdate = new HashMap<>();
                                userUpdate.put("followers",FieldValue.arrayRemove(myUid));
                                userUpdate.put("followings",FieldValue.arrayRemove(myUid));
                                userUpdate.put("blockedByArray",FieldValue.arrayUnion(myUid));
                                userUpdate.put("notificationOpenedUids",FieldValue.arrayRemove(myUid));
                                userUpdate.put("notificationOpenedByUids",FieldValue.arrayRemove(myUid));
                                DocumentReference documentReference1 = FirebaseFirestore.getInstance().collection("users").document(card.getFollowingId());
                                documentReference1.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        holder.unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = card.getUsername() + " kullanıcısını takipten çıkartmak istiyor musunuz?";
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Dikkat");
                builder.setMessage(messageText);
                builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HashMap<String,Object> myUpdate = new HashMap<>();
                        myUpdate.put("followings",FieldValue.arrayRemove(card.getFollowingId()));
                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(myUid);
                        documentReference.set(myUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HashMap<String,Object> userUpdate = new HashMap<>();
                                userUpdate.put("followers",FieldValue.arrayRemove(myUid));
                                DocumentReference documentReference1 = FirebaseFirestore.getInstance().collection("users").document(card.getFollowingId());
                                documentReference1.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Kullanıcı takipten çıkarıldı", Toast.LENGTH_SHORT).show();
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

        holder.followingsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("searchIdUsername",card.getFollowingId());
                parent.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return followingCards.size();
    }

    class PostHolder extends RecyclerView.ViewHolder {

        View followingsCard;
        TextView username;
        ImageView followingsPp;
        ImageView unfollow,block;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            unfollow = itemView.findViewById(R.id.followings_card_unfollow);
            username = itemView.findViewById(R.id.followings_card_username);
            block = itemView.findViewById(R.id.followings_card_block);
            followingsCard = itemView.findViewById(R.id.followingCard);
            followingsPp = itemView.findViewById(R.id.followings_card_profile_photo);

        }
    }

    public void filterList(ArrayList<FollowingCard> filteredList) {
        followingCards = filteredList;
        notifyDataSetChanged();
    }
}
