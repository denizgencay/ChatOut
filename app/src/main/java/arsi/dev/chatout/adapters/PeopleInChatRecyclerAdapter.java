package arsi.dev.chatout.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import arsi.dev.chatout.OthersProfileActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.SearchCard;
import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleInChatRecyclerAdapter extends RecyclerView.Adapter<PeopleInChatRecyclerAdapter.Holder> {

    private final int ADMIN_TYPE = 0;
    private final int USER_TYPE = 1;
    private ArrayList<SearchCard> people;
    private Context context;
    private ViewGroup parent;
    private String chatId;
    private ArrayList<String> blockedUsers;

    public PeopleInChatRecyclerAdapter(ArrayList<SearchCard> people, Context context, String chatId) {
        this.people = people;
        this.context = context;
        this.chatId = chatId;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_people_in_chat, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SearchCard card = people.get(position);

        Picasso.get().load(card.getProfilePhoto()).noFade().into(holder.profilePhoto);
        holder.username.setText(card.getUsername());
        if (!card.getSearchIdUsername().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && (blockedUsers != null && !blockedUsers.contains(card.getSearchIdUsername()))) {
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, OthersProfileActivity.class);
                    intent.putExtra("searchIdUsername", card.getSearchIdUsername());
                    parent.getContext().startActivity(intent);
                }
            });
        }

        if (holder.getItemViewType() == ADMIN_TYPE) {
            if (!card.getSearchIdUsername().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.block.setVisibility(View.VISIBLE);
                holder.block.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Dikkat");
                        builder.setMessage("Bu kullanıcıyı konuşmadan atmak istiyor musunuz?");
                        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                HashMap<String, Object> update = new HashMap<>();
                                update.put("peopleInChat", FieldValue.arrayRemove(card.getSearchIdUsername()));
                                update.put("blockedUsers", FieldValue.arrayUnion(card.getSearchIdUsername()));
                                FirebaseFirestore.getInstance().collection("chats").document(chatId).set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String, Object> userUpdate = new HashMap<>();
                                        userUpdate.put("activeChat", "");
                                        FirebaseFirestore.getInstance().collection("users").document(card.getSearchIdUsername()).set(userUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Kullanıcı konuşmadan çıkarıldı", Toast.LENGTH_SHORT).show();
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
            } else {
                holder.block.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    @Override
    public int getItemViewType(int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid.equals(people.get(position).getCreatorUid())) {
            return ADMIN_TYPE;
        } else {
            return USER_TYPE;
        }
    }

    public void setBlockedUsers(ArrayList<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public class Holder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        CircleImageView profilePhoto;
        TextView username, block;

        public Holder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.people_in_chat_card);
            profilePhoto = itemView.findViewById(R.id.people_in_chat_card_profile_photo);
            username = itemView.findViewById(R.id.people_in_chat_card_username);
            block = itemView.findViewById(R.id.people_in_chat_card_remove);
        }
    }
}
