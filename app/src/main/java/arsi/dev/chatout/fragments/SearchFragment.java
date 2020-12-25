package arsi.dev.chatout.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;

import java.util.List;

import arsi.dev.chatout.R;

public class SearchFragment extends Fragment {

    private TabLayout tabs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabs = view.findViewById(R.id.result_tabs);
        tabs.setupWithViewPager(viewPager);

        return view;
    }


    private void setupViewPager(ViewPager viewPager) {
        SearchFragment.Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new UsernameSearchFragment(), "Kullanıcı Adı");
        adapter.addFragment(new TitleSearchFragment(), "Başlık");

        viewPager.setAdapter(adapter);
    }

    class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            tabs.setTabTextColors(Color.parseColor("#BF0505"),Color.parseColor("#313131"));
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}

