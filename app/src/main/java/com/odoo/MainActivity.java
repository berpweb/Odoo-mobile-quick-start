package com.odoo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.odoo.addons.partners.PartnersFragment;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
    }

    public void setupViews() {
        loadFragment(PartnersFragment.newInstance(), PartnersFragment.TAG);
    }

    public void loadFragment(Fragment fragment, @Nullable String TAG) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flMain, fragment, TAG)
                .commit();
    }
}
