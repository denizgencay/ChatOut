package arsi.dev.chatout.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import arsi.dev.chatout.InsideEntryActivity;
import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.EntryCard;
import arsi.dev.chatout.cards.EntryInfoCard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class EntriesInfoRecyclerAdapter extends RecyclerView.Adapter<EntriesInfoRecyclerAdapter.PostHolder> {

    private ArrayList<EntryInfoCard> entryInfoCards;
    private Context context;
    private ViewGroup parent;

    public EntriesInfoRecyclerAdapter(ArrayList<EntryInfoCard> entryInfoCards, Context context) {
        this.entryInfoCards = entryInfoCards;
        this.context = context;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_entry_info,parent,false);
        return new PostHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        EntryInfoCard card = entryInfoCards.get(position);

        Date date = card.getTime().toDate();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String dateText = localDate.getDayOfMonth() + "." + localDate.getMonthValue() + "." + localDate.getYear();
        holder.date.setText(dateText);
        holder.entryTitleText.setText(card.getTitle());
        holder.entryInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InsideEntryActivity.class);
                intent.putExtra("entryId",card.getEntryId());
                parent.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entryInfoCards.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder {

        TextView entryTitleText, date;
        View entryInfoCard;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            entryInfoCard = itemView.findViewById(R.id.entryInfoCard);
            entryTitleText = itemView.findViewById(R.id.entryTitleText1);
            date = itemView.findViewById(R.id.entryDateText1);
        }
    }

    public void setType(ArrayList<EntryInfoCard> list) {
        entryInfoCards = list;
        notifyDataSetChanged();
    }
}
