package arsi.dev.chatout;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import arsi.dev.chatout.adapters.PremiumRecyclerAdapter;

public class PremiumActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private RecyclerView recyclerView;
    private PremiumRecyclerAdapter recyclerAdapter;
    private BillingClient billingClient;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        recyclerView = findViewById(R.id.premium_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        setupBillingClient();

    }

    private void loadProductsToRecyclerView(ArrayList<SkuDetails> skuDetails) {
        recyclerAdapter = new PremiumRecyclerAdapter(this, skuDetails, billingClient);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (billingClient.isReady()) {

                        List<String> skuList = new ArrayList<>();
                        skuList.add("premium_one_month_");

                        SkuDetailsParams subParams = SkuDetailsParams.newBuilder()
                                .setSkusList(skuList)
                                .setType(BillingClient.SkuType.SUBS)
                                .build();

//                        SkuDetailsParams lifetimeParams = SkuDetailsParams.newBuilder()
//                                .setSkusList(Arrays.asList("premium_life_time"))
//                                .setType(BillingClient.SkuType.INAPP)
//                                .build();

                        billingClient.querySkuDetailsAsync(subParams, new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    ArrayList<SkuDetails> skuDetails = (ArrayList<SkuDetails>) list;
                                    loadProductsToRecyclerView(skuDetails);
                                } else
                                    Toast.makeText(PremiumActivity.this, "Cannot query product", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else
                        Toast.makeText(PremiumActivity.this, "Not ready", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(PremiumActivity.this, "abc " + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (list != null && !list.isEmpty()) {
                String myUid = firebaseAuth.getCurrentUser().getUid();

                HashMap<String,Object> purchaseUpdate = new HashMap<>();
                purchaseUpdate.put("userId",myUid);
                DocumentReference documentReference = firebaseFirestore.collection("purchaseTokens").document(list.get(0).getPurchaseToken());
                documentReference.set(purchaseUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String,Object> update = new HashMap<>();
                        update.put("isPremium",true);
                        update.put("premiumPurchaseToken",list.get(0).getPurchaseToken());
                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(myUid);
                        documentReference1.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        });
                    }
                });
            }
        }
    }
}
