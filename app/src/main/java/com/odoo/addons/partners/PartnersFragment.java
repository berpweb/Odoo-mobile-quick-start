package com.odoo.addons.partners;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.odoo.AppBaseFragment;
import com.odoo.MainActivity;
import com.odoo.R;
import com.odoo.Utils;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.service.receivers.ISyncFinishReceiver;
import com.odoo.core.support.sync.SyncUtils;
import com.odoo.core.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import odoo.controls.BezelImageView;

public class PartnersFragment extends AppBaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = PartnersFragment.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rvPartners)
    SuperRecyclerView mPartners;

    ResPartner mResPartner;
    PartnersAdapter mAdapter;

    MainActivity mActivity;
    Handler mHandler;
    Utils mUtils;
    ISyncFinishReceiver syncFinishReceiver = new ISyncFinishReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Sync Finished
            mPartners.post(new Runnable() {
                @Override
                public void run() {
                    mPartners.setRefreshing(false);
                }
            });
            mAdapter.notifyDataSetChanged();
        }
    };

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
        mUtils = Utils.getInstance();
        mResPartner = new ResPartner(mActivity, user());
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

    @Override
    public void onResume() {
        super.onResume();
        mActivity.registerReceiver(syncFinishReceiver,
                new IntentFilter(ISyncFinishReceiver.SYNC_FINISH));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mActivity.unregisterReceiver(syncFinishReceiver);
        } catch (Exception e) {
            // Skipping issue related to unregister receiver
            e.printStackTrace();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mActivity, mResPartner.uri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        baseSetupPartners(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        baseSetupPartners(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, PartnersFragment.this);
    }

    public void setupViews() {
        mActivity.setSupportActionBar(mToolbar);
        mActivity.setTitle("Partners");
        setupPartners();
    }

    public SyncUtils sync() {
        return SyncUtils.get(mActivity);
    }

    public void setupPartners() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mPartners.setLayoutManager(linearLayoutManager);
        mPartners.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Log.e(TAG + " onRefresh", "onRefresh");
                // Sync Started
                sync().requestSync(ResPartner.AUTHORITY);
            }
        });
    }

    public void baseSetupPartners(Cursor cursor) {
        List<PartnersItem> partnersItems = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                String image = cursor.getString(cursor.getColumnIndex("image_small"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String companyName = cursor.getString(cursor.getColumnIndex("company_name"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                partnersItems.add(new PartnersItem(image, name, companyName, email));
            }
            mAdapter = new PartnersAdapter(partnersItems);
            mPartners.setAdapter(mAdapter);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        Log.e(TAG, "baseSetupPartners() called, partnersItems size is: " + partnersItems.size());
    }

    class PartnersAdapter extends RecyclerView.Adapter<PartnersViewHolder> {

        private List<PartnersItem> items;

        public PartnersAdapter(List<PartnersItem> items) {
            this.items = items;
        }

        @Override
        public PartnersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PartnersViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.partner_row_item, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(PartnersViewHolder holder, int position) {
            PartnersItem item = items.get(position);
            Bitmap bitmap;
            if (item.getImage().equals("false")) {
                bitmap = BitmapUtils.getAlphabetImage(
                        mActivity,
                        item.getName()
                );
            } else {
                bitmap = BitmapUtils.getAlphabetImage(
                        mActivity,
                        item.getImage()
                );
            }
            holder.getImage().setImageBitmap(bitmap);
            holder.getName().setText(item.getName());
            holder.getCompanyName().setText(
                    item.getCompanyName().equals("false")
                            ? "" : item.getCompanyName()
            );
            holder.getEmail().setText(
                    item.getEmail().equals("false")
                            ? "" : item.getEmail()
            );
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public List<PartnersItem> getItems() {
            return items;
        }
    }

    class PartnersViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_small)
        BezelImageView image;

        @BindView(R.id.name)
        TextView name;

        @BindView(R.id.company_name)
        TextView companyName;

        @BindView(R.id.email)
        TextView email;

        public PartnersViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public BezelImageView getImage() {
            return image;
        }

        public TextView getName() {
            return name;
        }

        public TextView getCompanyName() {
            return companyName;
        }

        public TextView getEmail() {
            return email;
        }
    }

    class PartnersItem {

        String image;
        String name;
        String companyName;
        String email;

        public PartnersItem(String image, String name, String companyName, String email) {
            this.image = image;
            this.name = name;
            this.companyName = companyName;
            this.email = email;
        }

        public String getImage() {
            return image;
        }

        public String getName() {
            return name;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getEmail() {
            return email;
        }
    }
}
