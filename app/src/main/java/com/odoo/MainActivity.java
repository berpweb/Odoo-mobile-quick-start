package com.odoo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.odoo.addons.partners.PartnersFragment;
import com.odoo.addons.partners.SyncFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.dlMain)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nvMain)
    NavigationView mNavigationView;

    AppCompatImageView mLogo;
    AppCompatTextView mHeader;
    AppCompatTextView mSubHeader;

    Prefs mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPrefs = new Prefs(this);
        setupNavDrawer();
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public NavigationView getNavigationView() {
        return mNavigationView;
    }

    public AppCompatImageView getLogo() {
        return mLogo;
    }

    public AppCompatTextView getHeader() {
        return mHeader;
    }

    public AppCompatTextView getSubHeader() {
        return mSubHeader;
    }

    public void setupNavDrawer() {
        Menu menu = mNavigationView.getMenu();
        menu.getItem(mPrefs.getSelectedPosition()).setChecked(true);
        loadFragment(mPrefs.getSelectedPosition());
        setUpUserData();
        setUpNavView();
    }

    public void setUpNavView() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                int selectedPosition = mPrefs.getSelectedPosition();
                switch (menuItem.getItemId()) {
                    case R.id.action_partners:
                        if (selectedPosition != 0) {
                            loadFragment(0);
                        }
                        break;
                    case R.id.action_sync:
                        if (selectedPosition != 1) {
                            loadFragment(1);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void setUpUserData() {
        View view = mNavigationView.getHeaderView(0);

        mLogo = (AppCompatImageView) view.findViewById(R.id.ivNavHeaderLogo);
        mHeader = (AppCompatTextView) view.findViewById(R.id.tvNavHeader);
        mSubHeader = (AppCompatTextView) view.findViewById(R.id.tvNavHeaderSubHeader);

        mHeader.setText("Kasim Rangwala");
        mSubHeader.setText("rangwalakasim@live.in");
    }

    public void loadFragment(int selectedPosition) {
        mPrefs.setSelectedPosition(selectedPosition);
        Fragment fragment = null;
        String TAG = null;
        if (selectedPosition == 0) {
            setTitle(getResources().getString(R.string.fragment_partners));
            fragment = PartnersFragment.newInstance();
            TAG = PartnersFragment.TAG;
        } else if (selectedPosition == 1) {
            setTitle(getResources().getString(R.string.fragment_sync));
            fragment = SyncFragment.newInstance();
            TAG = SyncFragment.TAG;
        }
        clearBackStack();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flMain, fragment, TAG)
                .commit();
    }

    public void clearBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStackImmediate();
        }
    }

    class Prefs {
        private static final String PrefsName = "navPrefs";
        private static final String SelectedPosition = "selectedPosition";

        SharedPreferences mPrefs;

        public Prefs(Context context) {
            super();
            mPrefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE);
        }

        public int getSelectedPosition() {
            return mPrefs.getInt(SelectedPosition, 0);
        }

        public void setSelectedPosition(int selectedPosition) {
            mPrefs.edit().putInt(SelectedPosition, selectedPosition).apply();
        }
    }
}
