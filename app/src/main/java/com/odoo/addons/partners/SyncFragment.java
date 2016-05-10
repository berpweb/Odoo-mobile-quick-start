package com.odoo.addons.partners;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.odoo.AppBaseFragment;
import com.odoo.MainActivity;
import com.odoo.R;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.base.addons.res.ResCountry;
import com.odoo.base.addons.res.ResCurrency;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SyncFragment extends AppBaseFragment {

    public static final String TAG = SyncFragment.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.cbSyncResCompany)
    AppCompatCheckBox mCompany;

    @BindView(R.id.cbSyncResCountry)
    AppCompatCheckBox mCountry;

    @BindView(R.id.cbSyncResCurrency)
    AppCompatCheckBox mCurrency;

    @BindView(R.id.cbSyncResPartner)
    AppCompatCheckBox mPartner;

    @BindView(R.id.cbSyncResUsers)
    AppCompatCheckBox mUsers;

    ActionBarDrawerToggle mDrawerToggle;

    MainActivity mActivity;

    ProgressDialog mSyncTableDialog;
    List<String> mCheckedTables;
    int mCurrentSyncingTableIndex;
    String mCurrentSyncingTable;

    public SyncFragment() {
        // Required empty public constructor
    }

    public static SyncFragment newInstance() {
        return new SyncFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sync, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setupViews();
    }

    @Override
    public void onSyncFinished() {
        super.onSyncFinished();
        syncTables();
    }

    public void setupViews() {
        mActivity.setSupportActionBar(mToolbar);

        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mDrawerToggle = new ActionBarDrawerToggle(
                mActivity,
                mActivity.getDrawerLayout(),
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mActivity.getDrawerLayout().addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }

    @OnClick({
            R.id.llSyncResCompany,
            R.id.llSyncResCountry,
            R.id.llSyncResCurrency,
            R.id.llSyncResPartner,
            R.id.llSyncResUsers
    })
    public void onSyncRes(LinearLayout layout) {
        switch (layout.getId()) {
            case R.id.llSyncResCompany:
                toggleCheck(mCompany);
                break;
            case R.id.llSyncResCountry:
                toggleCheck(mCountry);
                break;
            case R.id.llSyncResCurrency:
                toggleCheck(mCurrency);
                break;
            case R.id.llSyncResPartner:
                toggleCheck(mPartner);
                break;
            case R.id.llSyncResUsers:
                toggleCheck(mUsers);
                break;
        }
    }

    @OnClick(R.id.bnSync)
    public void onSync() {
        mCheckedTables = getCheckedTables();
        if (mCheckedTables.size() > 0) {
            mCurrentSyncingTableIndex = -1;
            syncTables();
        } else {
            utils().showMessage(
                    mActivity,
                    "Alert",
                    "Please, Select at least one table to sync.",
                    "OK"
            );
        }
    }

    public void toggleCheck(final AppCompatCheckBox checkBox) {
        checkBox.post(new Runnable() {
            @Override
            public void run() {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });
    }

    public List<String> getCheckedTables() {
        List<String> checkedTables = new ArrayList<>();

        if (mCompany.isChecked()) {
            checkedTables.add(ResCompany.AUTHORITY);
        }
        if (mCountry.isChecked()) {
            checkedTables.add(ResCountry.AUTHORITY);
        }
        if (mCurrency.isChecked()) {
            checkedTables.add(ResCurrency.AUTHORITY);
        }
        if (mPartner.isChecked()) {
            checkedTables.add(ResPartner.AUTHORITY);
        }
        if (mUsers.isChecked()) {
            checkedTables.add(ResUsers.AUTHORITY);
        }
        return checkedTables;
    }

    public void resetForm() {
        utils().resetForm((ViewGroup) getView());
    }

    public void syncTables() {
        if (mCurrentSyncingTableIndex == -1) {
            mSyncTableDialog = new ProgressDialog(mActivity, R.style.AppDialogTheme);
            mSyncTableDialog.setTitle("Syncing Tables");
            mSyncTableDialog.setCancelable(false);
            mSyncTableDialog.show();
        }
        mCurrentSyncingTableIndex++;
        if (mCheckedTables.size() > mCurrentSyncingTableIndex) {
            mSyncTableDialog.setMessage("Processing "
                    + (mCurrentSyncingTableIndex + 1)
                    + "/" + mCheckedTables.size()
                    + " tables."
            );
            mCurrentSyncingTable = mCheckedTables.get(mCurrentSyncingTableIndex);
            sync().requestSync(mCurrentSyncingTable);
        } else {
            if (mSyncTableDialog != null && mSyncTableDialog.isShowing()) {
                mSyncTableDialog.dismiss();
            }
            resetForm();
        }
    }
}
