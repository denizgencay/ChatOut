package arsi.dev.chatout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;


import arsi.dev.chatout.adapters.EntryRecyclerAdapter;
import arsi.dev.chatout.cards.EntryCard;
import arsi.dev.chatout.fragments.CurrentEntries;
import arsi.dev.chatout.fragments.FollowingEntries;
import arsi.dev.chatout.fragments.LastCommentedEntries;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class EntryActivity extends AppCompatActivity {

    private ArrayList<EntryCard> cards;
    private EntryRecyclerAdapter recyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<String> blockedUsers, blockedByUsers;
    private boolean isPremium;
    private TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        blockedByUsers = new ArrayList<>();
        blockedUsers = new ArrayList<>();
        cards = new ArrayList<>();

        ViewPager viewPager = findViewById(R.id.entry_viewpager);
        setupViewPager(viewPager);

        tabs = findViewById(R.id.entry_tabs);
        tabs.setupWithViewPager(viewPager);

        getDataFromFirestore();
    }

    public void openEntryAction(View view){
        if (isPremium) {
            Intent intent = new Intent(EntryActivity.this,CreateEntryActivity.class);
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Dikkat");
            builder.setMessage("Premium olmadan başlık açamazsınız!");
            builder.setNegativeButton("Tamam", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton("Premium Al", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(EntryActivity.this,PremiumActivity.class);
                    startActivity(intent);
                }
            });
            builder.show();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new LastCommentedEntries(),"Gündem");
        adapter.addFragment(new CurrentEntries(),"Güncel");
        adapter.addFragment(new FollowingEntries(),"Takip");
        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> mFragmentList = new ArrayList<>();
        private ArrayList<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String fragmentTitle) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(fragmentTitle);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            tabs.setTabTextColors(Color.parseColor("#BF0505"),Color.parseColor("#313131"));
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void getDataFromFirestore(){
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                isPremium = documentSnapshot.getBoolean("isPremium");
            }
        });
    }
}
