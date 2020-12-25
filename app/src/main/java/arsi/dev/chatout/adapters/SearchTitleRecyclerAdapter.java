package arsi.dev.chatout.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import arsi.dev.chatout.App;
import arsi.dev.chatout.ChangeModeratorActivity;
import arsi.dev.chatout.MessageActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.SearchTitleCard;
import arsi.dev.chatout.fragments.TitleSearchFragment;

public class SearchTitleRecyclerAdapter extends RecyclerView.Adapter<SearchTitleRecyclerAdapter.PostHolder> {

    private Context context;
    private ArrayList<SearchTitleCard> searchTitleCards;
    private boolean isCreator;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ViewGroup parent;
    private TitleSearchFragment titleSearchFragment;
    private String userActiveChat;
    private FragmentTransaction fragmentTransaction;
    private EditText editText;

    public SearchTitleRecyclerAdapter(ArrayList<SearchTitleCard> cards, Context context, TitleSearchFragment titleSearchFragment, FragmentTransaction fragmentTransaction, EditText editText) {
        this.searchTitleCards = cards;
        this.context = context;
        this.titleSearchFragment = titleSearchFragment;
        this.fragmentTransaction = fragmentTransaction;
        this.editText = editText;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_search_title, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchTitleRecyclerAdapter.PostHolder holder, int position) {
        SearchTitleCard card = searchTitleCards.get(position);

        Date date = card.getFinishTime().toDate();
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

        ArrayList<String> peopleInChat = card.getPeopleInChat();
        String personNumber = card.getPeopleInChat().size() + "/" + card.getPersonNumber();
        holder.time.setText(time);
        holder.personNumber.setText(personNumber);
        holder.searchTextView.setText(card.getTitle());

        boolean isContainsBlockedUser = false;

        for (String blockedUid : card.getBlockedUsers()) {
            if (peopleInChat.contains(blockedUid)) {
                isContainsBlockedUser = true;
                break;
            }
        }

        if (isContainsBlockedUser) holder.blockedView.setVisibility(View.VISIBLE);
        else holder.blockedView.setVisibility(View.GONE);

        holder.searchTitleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.searchTitleCard.setClickable(false);
                String uid = firebaseAuth.getCurrentUser().getUid();
                if (peopleInChat.size() < Integer.parseInt(card.getPersonNumber()) || peopleInChat.contains(uid)) {
                    if (!card.getActiveChat().equals(card.getSearchId()) && !card.getActiveChat().isEmpty()) {
                        if (isCreator) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Dikkat");
                            alertDialog.setMessage("Bulunduğunuz konuşmada moderatörsünüz, başka bir konuşmaya girmeden önce lütfen yeni bir moderatör belirleyin.");
                            alertDialog.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    holder.searchTitleCard.setClickable(true);
                                }
                            });
                            alertDialog.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    holder.searchTitleCard.setClickable(true);
                                    editText.setText("");
                                    fragmentTransaction.detach(titleSearchFragment).attach(titleSearchFragment).commit();
                                    Intent intent = new Intent(context, ChangeModeratorActivity.class);
                                    intent.putExtra("chatId", card.getActiveChat());
                                    intent.putExtra("newChatId", card.getSearchId());
                                    parent.getContext().startActivity(intent);
                                }
                            });
                            alertDialog.show();
                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Dikkat");
                            alertDialog.setMessage("Eğer yeni konuşmaya girerseniz eski konuşmadaki yerini kaybedeceksiniz");
                            alertDialog.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    holder.searchTitleCard.setClickable(true);
                                }
                            });
                            alertDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    HashMap<String, Object> newUpdate = new HashMap<>();
                                    newUpdate.put("peopleInChat", FieldValue.arrayUnion(uid));
                                    DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(card.getSearchId());
                                    documentReference1.set(newUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            App.chatChanging = true;
                                            String activeChat = card.getSearchId();
                                            Map<String, Object> data1 = new HashMap<>();
                                            data1.put("activeChat", activeChat);
                                            DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                                            documentReference.set(data1, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    String oldChatUid = card.getActiveChat();
                                                    HashMap<String, Object> oldUpdate = new HashMap<>();
                                                    oldUpdate.put("peopleInChat", FieldValue.arrayRemove(uid));
                                                    DocumentReference documentReference2 = firebaseFirestore.collection("chats").document(oldChatUid);
                                                    documentReference2.set(oldUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            editText.setText("");
                                                            fragmentTransaction.detach(titleSearchFragment).attach(titleSearchFragment).commit();
                                                            holder.searchTitleCard.setClickable(true);
                                                            Intent intent = new Intent(context, MessageActivity.class);
                                                            intent.putExtra("chatId", card.getSearchId());
                                                            parent.getContext().startActivity(intent);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            alertDialog.show();
                        }
                    } else {
                        if (card.getActiveChat().equals(card.getSearchId())) {
                            editText.setText("");
                            fragmentTransaction.detach(titleSearchFragment).attach(titleSearchFragment).commit();
                            holder.searchTitleCard.setClickable(true);
                            Intent intent = new Intent(context, MessageActivity.class);
                            intent.putExtra("chatId", card.getSearchId());
                            parent.getContext().startActivity(intent);
                        } else {
                            HashMap<String, Object> update = new HashMap<>();
                            update.put("peopleInChat", FieldValue.arrayUnion(uid));
                            DocumentReference documentReference = firebaseFirestore.collection("chats").document(card.getSearchId());
                            documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    String activeChat = card.getSearchId();
                                    HashMap<String, Object> userUpdate = new HashMap<>();
                                    userUpdate.put("activeChat", activeChat);
                                    DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
                                    documentReference1.set(userUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            editText.setText("");
                                            fragmentTransaction.detach(titleSearchFragment).attach(titleSearchFragment).commit();
                                            holder.searchTitleCard.setClickable(true);
                                            Intent intent = new Intent(context, MessageActivity.class);
                                            intent.putExtra("chatId", card.getSearchId());
                                            parent.getContext().startActivity(intent);
                                        }
                                    });
                                }
                            });
                        }
                    }
                } else {
                    holder.searchTitleCard.setClickable(true);
                    Toast.makeText(context, "Bu konuşma doludur.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchTitleCards.size();
    }

    public void filterList(ArrayList<SearchTitleCard> filteredList) {
        searchTitleCards = filteredList;
        notifyDataSetChanged();
    }

    public void setType(ArrayList<SearchTitleCard> list) {
        searchTitleCards = list;
        notifyDataSetChanged();
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    public void setUserActiveChat(String userActiveChat) {
        this.userActiveChat = userActiveChat;
    }

    class PostHolder extends RecyclerView.ViewHolder {

        View searchTitleCard;
        TextView searchTextView, personNumber, time;
        RelativeLayout blockedView;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            searchTextView = itemView.findViewById(R.id.searchTextViewTitle);
            searchTitleCard = itemView.findViewById(R.id.searchTitleCardView);
            personNumber = itemView.findViewById(R.id.title_person_number);
            time = itemView.findViewById(R.id.title_time);
            blockedView = itemView.findViewById(R.id.search_title_card_blocked_view);
        }
    }
}
