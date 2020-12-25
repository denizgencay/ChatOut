package arsi.dev.chatout.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;


import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.OthersProfileActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.SearchCard;
import arsi.dev.chatout.fragments.UsernameSearchFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.PostHolder> {

    private Context context;
    private ArrayList<SearchCard> searchCards;
    private ArrayList<String> lastSearchUids;
    private UsernameSearchFragment fragment;
    private ViewGroup parent;
    private EditText searchBar;

    public SearchRecyclerAdapter(ArrayList<SearchCard> cards, Context context, UsernameSearchFragment fragment, EditText searchBar) {
        this.searchCards = cards;
        this.context = context;
        this.fragment = fragment;
        this.searchBar = searchBar;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_search,parent,false);

        return new PostHolder(view);
    }

    public void setType(ArrayList<SearchCard> list){
        searchCards = list;
        notifyDataSetChanged();
    }

    public void setLastSearchUids(ArrayList<String> lastSearchUids) {
        this.lastSearchUids = lastSearchUids;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRecyclerAdapter.PostHolder holder, int position) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final SearchCard card =  searchCards.get(position);
        holder.searchTextView.setText(card.getUsername());
        holder.searchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.searchCard.setClickable(false);
                if (lastSearchUids != null) {
                    Collections.reverse(lastSearchUids);
                }

                boolean alreadySearched = false;

                if (lastSearchUids != null) {
                    for (String lastSearchedUid : lastSearchUids) {
                        if (lastSearchedUid.equals(card.getSearchIdUsername())) {
                            alreadySearched = true;
                            break;
                        }
                    }
                }

                if (alreadySearched) {
                    lastSearchUids.remove(card.getSearchIdUsername());
                    lastSearchUids.add(card.getSearchIdUsername());

                    HashMap<String,Object> update = new HashMap<>();
                    update.put("lastSearchs",lastSearchUids);

                    DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(currentUserUid);

                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            searchBar.setText("");
                            searchBar.clearFocus();
                            holder.searchCard.setClickable(true);
                            Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                            intent.putExtra("searchIdUsername",card.getSearchIdUsername());
                            fragment.startActivityForResult(intent,1);
                        }
                    });
                } else {
                    if (lastSearchUids == null) {
                        lastSearchUids = new ArrayList<>();
                    }

                    if (lastSearchUids.size() == 10) {
                        lastSearchUids.remove(0);
                        lastSearchUids.add(card.getSearchIdUsername());

                        HashMap<String,Object> update = new HashMap<>();
                        update.put("lastSearchs",lastSearchUids);
                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(currentUserUid);

                        documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                searchBar.setText("");
                                searchBar.clearFocus();
                                holder.searchCard.setClickable(true);
                                Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                                intent.putExtra("searchIdUsername",card.getSearchIdUsername());
                                fragment.startActivityForResult(intent,1);
                            }
                        });

                    } else {
                        lastSearchUids.add(card.getSearchIdUsername());

                        HashMap<String,Object> update = new HashMap<>();
                        update.put("lastSearchs",lastSearchUids);
                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(currentUserUid);

                        documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                searchBar.setText("");
                                searchBar.clearFocus();
                                holder.searchCard.setClickable(true);
                                Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                                intent.putExtra("searchIdUsername",card.getSearchIdUsername());
                                fragment.startActivityForResult(intent,1);
                            }
                        });
                    }
                }
            }
        });
        Picasso.get().load(card.getProfilePhoto()).noFade().into(holder.profilePhoto);
    }

    @Override
    public int getItemCount() {
        return searchCards.size();
    }

    public void filterList(ArrayList<SearchCard> filteredList){
        searchCards = filteredList;
        notifyDataSetChanged();
    }

    static class PostHolder extends RecyclerView.ViewHolder{
        RelativeLayout searchCard;
        TextView searchTextView;
        CircleImageView profilePhoto;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            searchCard  = itemView.findViewById(R.id.searchCardView);
            searchTextView = itemView.findViewById(R.id.searchTextView);
            profilePhoto = itemView.findViewById(R.id.search_card_profile_image);
        }
    }
}