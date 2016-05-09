package com.odoo.addons.partners;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.odoo.AppBaseFragment;
import com.odoo.MainActivity;
import com.odoo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PartnersFragment extends AppBaseFragment {

    public static final String TAG = PartnersFragment.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rvPartners)
    SuperRecyclerView mPartners;

    MainActivity mActivity;
    Handler mHandler;

    public PartnersFragment() {
        // Required empty public constructor
    }

    public static PartnersFragment newInstance() {
        return new PartnersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_partners, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setupViews();
    }

    public void setupViews() {
        mActivity.setSupportActionBar(mToolbar);
        mActivity.setTitle("Partners");
    }
}
