package arsi.dev.chatout.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import arsi.dev.chatout.App;
import arsi.dev.chatout.ChangeModeratorActivity;
import arsi.dev.chatout.MessageActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.ChatCard;
import arsi.google.android.ads.nativetemplates.TemplateView;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int CHAT_VIEW = 0;
    private final int AD_VIEW = 1;
    private ArrayList<ChatCard> cards;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String activeChat1;
    private boolean isCreator;
    private ViewGroup parent;

    public RecyclerAdapter(ArrayList<ChatCard> cards, Context context) {
        this.cards = cards;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        RecyclerView.ViewHolder viewHolder;
        if (viewType == CHAT_VIEW) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.recycler_row, parent, false);
            viewHolder = new PostHolder(view);
            return viewHolder;
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.admob_view, parent, false);
            viewHolder = new AdHolder(view);
            return viewHolder;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        String uid = firebaseAuth.getCurrentUser().getUid();
        int viewType = holder.getItemViewType();

        if (viewType == CHAT_VIEW) {
            ChatCard card = cards.get(position);
            Date date = card.getTime().toDate();
            LocalTime localtime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            String time = "";
            if (localtime.getHour() < 10) {
                if (localtime.getMinute() < 10) {
                    time = "0" + localtime.getHour() + ":" + "0" + localtime.getMinute();
                } else {
                    time = "0" + localtime.getHour() + ":" + localtime.getMinute();
                }
            } else {
                if (localtime.getMinute() < 10) {
                    time = localtime.getHour() + ":" + "0" + localtime.getMinute();
                } else {
                    time = localtime.getHour() + ":" + localtime.getMinute();
                }
            }

            ArrayList<String> peopleInChat = card.getPeopleInChat();
            ((PostHolder) holder).titleText.setText(card.getTitle());
            String numberText = peopleInChat.size() + "/" + card.getNumberOfPerson();
            ((PostHolder) holder).numberText.setText(numberText);
            ((PostHolder) holder).timeText.setText(time);

            boolean isContainsBlockedUser = false;

            for (String blockedUid : card.getBlockedUsers()) {
                if (peopleInChat.contains(blockedUid)) {
                    isContainsBlockedUser = true;
                    break;
                }
            }

            if (isContainsBlockedUser)
                ((PostHolder) holder).blockedView.setVisibility(View.VISIBLE);
            else ((PostHolder) holder).blockedView.setVisibility(View.GONE);

            ((PostHolder) holder).chatCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PostHolder) holder).chatCard.setClickable(false);
                    if (peopleInChat.size() < Integer.parseInt(card.getNumberOfPerson()) || peopleInChat.contains(uid)) {
                        if (!card.getActiveChat1().equals(card.getChatId()) && !card.getActiveChat1().isEmpty()) {
                            if (isCreator) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                                alertDialog.setTitle("Dikkat");
                                alertDialog.setMessage("Bulunduğunuz konuşmada moderatörsünüz, başka bir konuşmaya girmeden önce lütfen yeni bir moderatör belirleyin.");
                                alertDialog.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((PostHolder) holder).chatCard.setClickable(true);
                                    }
                                });
                                alertDialog.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((PostHolder) holder).chatCard.setClickable(true);
                                        Intent intent = new Intent(context, ChangeModeratorActivity.class);
                                        intent.putExtra("chatId", card.getActiveChat1());
                                        intent.putExtra("newChatId", card.getChatId());
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
                                        ((PostHolder) holder).chatCard.setClickable(true);
                                    }
                                });
                                alertDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        HashMap<String, Object> newUpdate = new HashMap<>();
                                        newUpdate.put("peopleInChat", FieldValue.arrayUnion(uid));
                                        DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(card.getChatId());
                                        documentReference1.set(newUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                App.chatChanging = true;
                                                String activeChat = card.getChatId();
                                                Map<String, Object> data1 = new HashMap<>();
                                                data1.put("activeChat", activeChat);
                                                DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                                                documentReference.set(data1, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        String oldChatUid = card.getActiveChat1();
                                                        HashMap<String, Object> oldUpdate = new HashMap<>();
                                                        oldUpdate.put("peopleInChat", FieldValue.arrayRemove(uid));
                                                        DocumentReference documentReference2 = firebaseFirestore.collection("chats").document(oldChatUid);
                                                        documentReference2.set(oldUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                ((PostHolder) holder).chatCard.setClickable(true);
                                                                Intent intent = new Intent(context, MessageActivity.class);
                                                                intent.putExtra("chatId", card.getChatId());
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
                            if (card.getActiveChat1().equals(card.getChatId())) {
                                ((PostHolder) holder).chatCard.setClickable(true);
                                Intent intent = new Intent(context, MessageActivity.class);
                                intent.putExtra("chatId", card.getChatId());
                                parent.getContext().startActivity(intent);
                            } else {
                                HashMap<String, Object> update = new HashMap<>();
                                update.put("peopleInChat", FieldValue.arrayUnion(uid));
                                DocumentReference documentReference = firebaseFirestore.collection("chats").document(card.getChatId());
                                documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        String activeChat = card.getChatId();
                                        HashMap<String, Object> userUpdate = new HashMap<>();
                                        userUpdate.put("activeChat", activeChat);
                                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(uid);
                                        documentReference1.set(userUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                ((PostHolder) holder).chatCard.setClickable(true);
                                                Intent intent = new Intent(context, MessageActivity.class);
                                                intent.putExtra("chatId", card.getChatId());
                                                parent.getContext().startActivity(intent);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(context, "Bu konuşma doludur.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            AdLoader adLoader = new AdLoader.Builder(context, "ca-app-pub-3940256099942544/1044960115")
                    .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            ((AdHolder) holder).adView.setNativeAd(unifiedNativeAd);
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // Handle the failure by logging, altering the UI, and so on.
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder()
                            // Methods in the NativeAdOptions.Builder class can be
                            // used here to specify individual options settings.
                            .build())
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position).getChatId() == null) {
            return AD_VIEW;
        } else {
            return CHAT_VIEW;
        }
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    public void setType(ArrayList<ChatCard> list) {
        this.cards = list;
        notifyDataSetChanged();
    }

    class PostHolder extends RecyclerView.ViewHolder {

        View chatCard;
        TextView titleText;
        TextView numberText;
        TextView timeText;
        RelativeLayout blockedView;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            numberText = itemView.findViewById(R.id.personNumber);
            titleText = itemView.findViewById(R.id.titleView);
            timeText = itemView.findViewById(R.id.timeText);
            chatCard = itemView.findViewById(R.id.chatCardView);
            blockedView = itemView.findViewById(R.id.chat_card_blocked_view);
        }
    }

    class AdHolder extends RecyclerView.ViewHolder {

        RelativeLayout adCard;
        TemplateView adView;

        public AdHolder(@NonNull View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.admob);
            adCard = itemView.findViewById(R.id.admob_row);
        }
    }
}
