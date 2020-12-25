package arsi.dev.chatout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import arsi.dev.chatout.fragments.HomeFragment;
import arsi.dev.chatout.fragments.ProfileFragment;
import arsi.dev.chatout.fragments.SearchFragment;

public class MainPage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    int selectedId = 3;

                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            selectedId = 0;
                            break;
                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            selectedId = 1;
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            selectedId = 2;
                            break;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (i != selectedId) {
                            bottomNavigationView.getMenu().getItem(i).setEnabled(true);
                        }
                    }
                    bottomNavigationView.getMenu().getItem(selectedId).setEnabled(false);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        bottomNavigationView.getMenu().getItem(0).setEnabled(false);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null && bundle.getString("key") != null) {
            redirect(bundle);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        if (bundle != null && bundle.getString("key") != null) {
            redirect(bundle);
        }
    }

    private void redirect(Bundle bundle) {
        if (bundle != null) {
            switch (bundle.getString("key")) {
                case "friendRequest":
                case "createChat":
                    Intent intent = new Intent(this,OthersProfileActivity.class);
                    intent.putExtra("searchIdUsername",bundle.getString("userId"));
                    startActivity(intent);
                    break;
                case "newMessage":
                    Intent intent1 = new Intent(this,MessageActivity.class);
                    intent1.putExtra("chatId",bundle.getString("chatId"));
                    startActivity(intent1);
                    break;
            }
        }
    }
}