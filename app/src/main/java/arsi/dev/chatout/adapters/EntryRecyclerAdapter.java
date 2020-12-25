package arsi.dev.chatout.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.InsideEntryActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.EntryCard;
import arsi.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nonnull;

public class EntryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<EntryCard> entryCards;
    private Context context;

    private final int ENTRY_CARD = 0;
    private final int AD_CARD = 1;

    public EntryRecyclerAdapter(ArrayList<EntryCard> entryCards, Context context) {
        this.entryCards = entryCards;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ENTRY_CARD) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.recycler_row_entry,parent,false);
            return new Entry(view);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.admob_view,parent,false);
            return new AdView(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EntryCard card = entryCards.get(position);

        if (holder.getItemViewType() == ENTRY_CARD) {
            Date date = card.getTime().toDate();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String dateText = localDate.getDayOfMonth() + "." + localDate.getMonthValue() + "." + localDate.getYear();

            ((Entry)holder).commentNumberText.setText(card.getCommentNumber());
            ((Entry)holder).entryTitleText.setText(card.getTitle());
            ((Entry)holder).date.setText(dateText);
            ((Entry)holder).entryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, InsideEntryActivity.class);
                    intent.putExtra("entryId",card.getEntryId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        } else {
            AdLoader adLoader = new AdLoader.Builder(context, "ca-app-pub-3940256099942544/1044960115")
                    .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            ((AdView)holder).admob.setNativeAd(unifiedNativeAd);
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
        return entryCards.size();
    }

    public void filterList(ArrayList<EntryCard> filteredList) {
        entryCards = filteredList;
        notifyDataSetChanged();
    }

    public class Entry extends RecyclerView.ViewHolder {

        TextView entryTitleText, commentNumberText, date;
        View entryCard;

        public Entry(@NonNull View itemView) {
            super(itemView);
            entryTitleText = itemView.findViewById(R.id.entryTitleText);
            commentNumberText = itemView.findViewById(R.id.entryCommentNumber);
            entryCard = itemView.findViewById(R.id.entryCard);
            date = itemView.findViewById(R.id.entryDateText);
        }
    }

    public class AdView extends RecyclerView.ViewHolder {

        RelativeLayout card;
        TemplateView admob;

        public AdView(@Nonnull View itemView) {
            super(itemView);

            admob = itemView.findViewById(R.id.admob);
            card = itemView.findViewById(R.id.admob_row);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (entryCards.get(position).getEntryId() != null) {
            return ENTRY_CARD;
        } else {
            return AD_CARD;
        }
    }
}
