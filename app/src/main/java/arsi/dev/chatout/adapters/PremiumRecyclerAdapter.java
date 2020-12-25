package arsi.dev.chatout.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;

import java.util.ArrayList;

import arsi.dev.chatout.PremiumActivity;
import arsi.dev.chatout.R;

public class PremiumRecyclerAdapter extends RecyclerView.Adapter<PremiumRecyclerAdapter.Holder> {

    private PremiumActivity premiumActivity;
    private ArrayList<SkuDetails> skuDetails;
    private BillingClient billingClient;

    public PremiumRecyclerAdapter(PremiumActivity premiumActivity, ArrayList<SkuDetails> skuDetails, BillingClient billingClient) {
        this.premiumActivity = premiumActivity;
        this.skuDetails = skuDetails;
        this.billingClient = billingClient;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_premium_card,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.name.setText(skuDetails.get(position).getTitle());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails.get(position))
                        .build();
                BillingResult result = billingClient.launchBillingFlow(premiumActivity,billingFlowParams);
                System.out.println(result.getResponseCode());
            }
        });
    }

    @Override
    public int getItemCount() {
        return skuDetails.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.premium_card_name);
            card = itemView.findViewById(R.id.preium_card);
        }
    }
}
