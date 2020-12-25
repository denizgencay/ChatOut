package arsi.dev.chatout.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.BlockedCard;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockedUsersRecyclerAdapter extends RecyclerView.Adapter<BlockedUsersRecyclerAdapter.Holder> {

    private ArrayList<BlockedCard> blockedUsers;
    private Context context;
    private ViewGroup parent;

    public BlockedUsersRecyclerAdapter(ArrayList<BlockedCard> blockedUsers, Context context) {
        this.blockedUsers = blockedUsers;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_blocked_user,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BlockedCard card = blockedUsers.get(position);
        holder.username.setText(card.getUsername());
        holder.unBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String,Object> myUpdate = new HashMap<>();
                myUpdate.put("blockedArray", FieldValue.arrayRemove(card.getUserId()));
                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(myUid);
                documentReference.set(myUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String,Object> userUpdate = new HashMap<>();
                        userUpdate.put("blockedByArray",FieldValue.arrayRemove(myUid));
                        DocumentReference documentReference1 = FirebaseFirestore.getInstance().collection("users").document(card.getUserId());
                        documentReference1.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(parent.getContext(), "Kullanıcının engeli kaldırıldı", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return blockedUsers.size();
    }

    public void filterList(ArrayList<BlockedCard> filteredList) {
        blockedUsers = filteredList;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView username,unBlock;

        public Holder(@NonNull View itemView) {
            super(itemView);

            unBlock = itemView.findViewById(R.id.blocked_user_unblock);
            username = itemView.findViewById(R.id.blocked_user_card_username);
        }
    }
}
