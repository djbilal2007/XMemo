package com.app.xmemo.xmemo_image.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.app.xmemo.xmemo_image.R;
import com.app.xmemo.xmemo_image.fragment.ContactsFragment;
import com.app.xmemo.xmemo_image.fragment.GalleryFragment;
import com.app.xmemo.xmemo_image.fragment.SettingsFragment;
import com.app.xmemo.xmemo_image.utils.Utils;

public class UserHomeActivity extends AppCompatActivity {

    private BottomNavigationView navigationView;
    private Fragment fragment;
    private FragmentManager fragmentManager;

    private ViewPager viewPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

      /*  tabLayout = (TabLayout) findViewById(R.id.tabLayout_bottom);
        viewPager = (ViewPager) findViewById(R.id.viewPager_user_home);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        reduceMarginsInTabs(tabLayout, 60);
    }

    public static void reduceMarginsInTabs(TabLayout tabLayout, int marginOffset) {

        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            for (int i = 0; i < ((ViewGroup) tabStrip).getChildCount(); i++) {
                View tabView = tabStripGroup.getChildAt(i);
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).leftMargin = marginOffset;
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).rightMargin = marginOffset;
                }
            }
            tabLayout.requestLayout();
        }
    }

    private void setupTabIcons() {
        View view1 =  LayoutInflater.from(this).inflate(R.layout.customtab, null);
        TextView tabOne = (TextView)view1.findViewById(R.id.tabName_tablayout);
        ImageView iv1 = (ImageView)view1.findViewById(R.id.imageview_tabLayout);
        tabOne.setText("Gallery");
        iv1.setImageResource(R.mipmap.gallery_icon);
        tabLayout.getTabAt(0).setCustomView(view1);


        //view.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_gallery, 0, 0);

        View view2 =  LayoutInflater.from(this).inflate(R.layout.customtab, null);
        TextView tabTwo = (TextView)view2.findViewById(R.id.tabName_tablayout);
        ImageView iv2 = (ImageView)view2.findViewById(R.id.imageview_tabLayout);
        tabTwo.setText("Contacts");
        iv2.setImageResource(R.mipmap.contact_icon);
        tabLayout.getTabAt(1).setCustomView(view2);

        View view3 =  LayoutInflater.from(this).inflate(R.layout.customtab, null);
        TextView tabThree = (TextView)view3.findViewById(R.id.tabName_tablayout);
        ImageView iv3 = (ImageView)view3.findViewById(R.id.imageview_tabLayout);
        tabThree.setText("Settings");
        iv3.setImageResource(R.mipmap.setting_icon);
        tabLayout.getTabAt(2).setCustomView(view3);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new GalleryFragment(), "Gallery");
        adapter.addFrag(new ContactsFragment(), "Contacts");
        adapter.addFrag(new SettingsFragment(), "Settings");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        //
        private Fragment fragment;
        //

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                if(fragment == null){
                    fragment = GalleryFragment.
                }
            }

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public interface FirstPageFragmentListener
    {
        void onSwitchToNextFragment();
    }*/

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, new GalleryFragment())
                .commit();

        navigationView = (BottomNavigationView) findViewById(R.id.navigationMenu);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_gallery:
                        fragment = new GalleryFragment();
                        break;

                    case R.id.navigation_contact:
                        fragment = new ContactsFragment();
                        break;

                    case R.id.navigation_settings:
                        fragment = new SettingsFragment();
                        break;
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .commit();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        Utils.exitApp(this);
    }
}
