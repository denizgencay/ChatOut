package arsi.dev.chatout.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import arsi.dev.chatout.CreateChat;
import arsi.dev.chatout.EntryActivity;

import arsi.dev.chatout.R;
import arsi.dev.chatout.cards.ChatCard;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<ChatCard> cards;
    private ImageView createChatButton,goToEntriesButton;
    private TabLayout tabs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home,container,false);
        ViewPager viewPager = view.findViewById(R.id.home_viewpager);
        setupViewPager(viewPager);

        tabs = view.findViewById(R.id.home_tabs);
        tabs.setupWithViewPager(viewPager);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        cards = new ArrayList<>();
        createChatButton = view.findViewById(R.id.createChatButton);
        goToEntriesButton = view.findViewById(R.id.goToEntriesButton);
        createChatButtonControl();
        goToEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EntryActivity.class);
                startActivity(intent);
            }
        });

       return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new HomeCurrentChats(),"Güncel");
        adapter.addFragment(new HomeFollowingsChats(),"Takip Ettiklerim");
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

    public void createChatButtonControl(){
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String activeChat = (String) documentSnapshot.get("activeChat");
                if (activeChat != null) {
                    if(!activeChat.isEmpty()){
                        createChatButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                alert.setTitle("Dikkat");
                                alert.setMessage("Eğer yeni konuşma açarsanız eski konuşmadaki yerini kaybedeceksiniz");
                                alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                alert.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getContext(), CreateChat.class);
                                        getActivity().startActivity(intent);
                                    }
                                });
                                alert.show();
                            }
                        });
                    }else{
                        createChatButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), CreateChat.class);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });
    }
}
