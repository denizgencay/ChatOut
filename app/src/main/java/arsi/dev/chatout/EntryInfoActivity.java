package arsi.dev.chatout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;


import arsi.dev.chatout.fragments.Comments;
import arsi.dev.chatout.fragments.Entries;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class EntryInfoActivity extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_info);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        ViewPager viewPager = findViewById(R.id.entry_info_viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = findViewById(R.id.entry_info__tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new Entries(userId),"Başlıklar");
        adapter.addFragment(new Comments(userId),"Yorumlar");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
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
}
