package com.piford.amessenger;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.piford.amessenger.fragmentss.CallFragment;
import com.piford.amessenger.fragmentss.MessageFragment;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    ArrayList<Fragment> fragmentList = new ArrayList<Fragment> ( );

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_userlist);
        tabLayout = findViewById (R.id.tabLayout);
        viewPager = findViewById (R.id.viewPager);
        fragmentList.add (new MessageFragment ( ));
        fragmentList.add (new CallFragment ( ));
        MyPagerAdapter adapter = new MyPagerAdapter (getSupportFragmentManager ( ));
        viewPager.setAdapter (adapter);
        tabLayout.post (new Runnable ( ) {
            @Override
            public void run () {
                tabLayout.setupWithViewPager (viewPager);
            }
        });
        getSupportActionBar ( ).setTitle (FirebaseAuth.getInstance ( ).getCurrentUser ( ).getPhoneNumber ( ));
//        getSupportFragmentManager ( ).beginTransaction ( ).replace (R.id.frame, new MessageFragment ( )).commit ( );

    }

    class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter (FragmentManager manager) {
            super (manager);
        }

        @Override
        public Fragment getItem (int position) {
            return fragmentList.get (position);
        }

        @Override
        public int getCount () {
            return fragmentList.size ( );
        }

        @Nullable
        @Override
        public CharSequence getPageTitle (int position) {
            if (position == 0) {
                return "Messages";
            } else if (position == 1) {
                return "Calls";
            }
            return super.getPageTitle (position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        menu.add ("Logout");
        return super.onCreateOptionsMenu (menu);
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        if (item.getTitle ( ).equals ("Logout")) {
            FirebaseAuth.getInstance ( ).signOut ( );
            finish ( );
        }
        return super.onOptionsItemSelected (item);
    }

}
