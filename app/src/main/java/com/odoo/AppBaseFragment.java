package com.odoo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.odoo.core.service.receivers.ISyncFinishReceiver;
import com.odoo.core.support.OUser;
import com.odoo.core.support.sync.SyncUtils;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.OdooError;

public class AppBaseFragment extends Fragment {

    public static final String TAG = AppBaseFragment.class.getSimpleName();

    private Odoo mOdoo;
    private Handler mHandler;
    private Utils mUtils;

    private AppCompatActivity mActivity;
    private AlertDialog mNetworkAlert;
    private AlertDialog mLocationAlert;
    private boolean isOdooAuthenticated = false;

    private ISyncFinishReceiver syncFinishReceiver = new ISyncFinishReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Sync Finished
            onSyncFinished();
        }
    };

    public AppBaseFragment() {
        // Required empty public constructor
    }

    /**
     * onSyncFinished will get called when syncService get destroyed.
     */
    public void onSyncFinished() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (AppCompatActivity) getActivity();
        mHandler = new Handler();
        mUtils = Utils.getInstance();
        baseCreateOdooInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        baseCreateOdooInstance(false);
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

    public void baseCreateOdooInstance(boolean createOdooInstance) {
        if (null != mNetworkAlert && mNetworkAlert.isShowing()) {
            mNetworkAlert.dismiss();
        }

        if (null != mLocationAlert && mLocationAlert.isShowing()) {
            mLocationAlert.dismiss();
        }

        if (!mUtils.isDeviceOnline(mActivity)) {
            mNetworkAlert = mUtils.showMessage(mActivity,
                    mActivity.getString(R.string.network_alert_title),
                    mActivity.getString(R.string.network_alert_message),
                    "OK");
        } else {
            if (createOdooInstance) {
                createOdooInstance();
            }
        }
        mLocationAlert = mUtils.checkLocationSettings(mActivity);
    }

    public Utils utils() {
        return mUtils;
    }

    public SyncUtils sync() {
        return SyncUtils.get(mActivity);
    }

    public OUser user() {
        try {
            return OUser.current(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Odoo getOdoo() {
        if (mOdoo == null) {
            Log.e(TAG, "Odoo is null");
        } else if (!isOdooAuthenticated) {
            Log.e(TAG, "Odoo isn't authenticated yet.");
        }
        return mOdoo;
    }

    public boolean isOdooAuthenticated() {
        return isOdooAuthenticated;
    }

    public void createOdooInstance() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final OUser user = user();
                if (user != null) {
                    try {
                        Odoo.createInstance(mActivity, user.getHost())
                                .setOnConnect(new IOdooConnectionListener() {
                                    @Override
                                    public void onConnect(Odoo odoo) {
                                        if (odoo != null) {
                                            odoo.authenticate(user.getUsername(), user.getPassword(),
                                                    user().getDatabase(), new IOdooLoginCallback() {
                                                        @Override
                                                        public void onLoginSuccess(Odoo odoo, odoo.helper.OUser oUser) {
                                                            mOdoo = odoo;
                                                            isOdooAuthenticated = true;
                                                            Log.i(TAG, "Odoo is: " + mOdoo.toString());
                                                        }

                                                        @Override
                                                        public void onLoginFail(OdooError odooError) {
                                                            if (odooError != null) {
                                                                mUtils.showMessage(mActivity, "Login Failed!",
                                                                        String.valueOf(odooError.getMessage()),
                                                                        "Retry", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                mActivity.recreate();
                                                                            }
                                                                        },
                                                                        "Cancel", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                mActivity.finish();
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onError(OdooError odooError) {
                                        if (odooError != null) {
                                            mUtils.showMessage(mActivity, "Server Error",
                                                    String.valueOf(odooError.getMessage()),
                                                    "Retry", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            mActivity.recreate();
                                                        }
                                                    },
                                                    "Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            mActivity.finish();
                                                        }
                                                    });
                                        }
                                    }
                                });
                    } catch (OdooVersionException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Odoo Instance failed to create. user is null");
                }
            }
        });
    }
}
