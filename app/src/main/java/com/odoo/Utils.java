package com.odoo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.odoo.addons.partners.backend.LocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;

public class Utils {
    private static Utils ourInstance = new Utils();

    private Utils() {
    }

    public static Utils getInstance() {
        return ourInstance;
    }

    public boolean isDeviceOnline(@NonNull Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void resetForm(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            final View view = group.getChildAt(i);
            if (view instanceof EditText) {
                final EditText editText = (EditText) view;
                editText.post(new Runnable() {
                    @Override
                    public void run() {
                        editText.getText().clear();
                    }
                });
            }

            if (view instanceof RadioGroup) {
                final RadioButton radioButton = ((RadioButton) ((RadioGroup) view).getChildAt(0));
                radioButton.post(new Runnable() {
                    @Override
                    public void run() {
                        radioButton.setChecked(true);
                    }
                });
                ((RadioButton) ((RadioGroup) view).getChildAt(0)).setChecked(true);
            }

            if (view instanceof Spinner) {
                final Spinner spinner = (Spinner) view;
                spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setSelection(0);
                    }
                });
            }

            if (view instanceof CompoundButton && !(view instanceof RadioButton)) {
                final CompoundButton button = (CompoundButton) view;
                button.post(new Runnable() {
                    @Override
                    public void run() {
                        button.setChecked(false);
                    }
                });
            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                resetForm((ViewGroup) view);
        }
    }

    public String getCurrentDate() {
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH) + 1;
        int year = now.get(Calendar.YEAR);

        return ("" + year) + "-"
                + (month < 10 ? ("0" + month) : ("" + month)) + "-"
                + (dayOfMonth < 10 ? ("0" + dayOfMonth) : ("" + dayOfMonth));
    }

    public void getStackTrace(final Class<?> cls) {
        final String className = cls.getName();
        final String classSimpleName = cls.getSimpleName();
        final StackTraceElement[] steArray = Thread.currentThread().getStackTrace();
        int index = 0;
        for (StackTraceElement ste : steArray) {
            if (ste.getClassName().equals(className)) {
                break;
            }
            index++;
        }
        if (index >= steArray.length) {
            // Little Hacky
            Log.w(classSimpleName,
                    Arrays.toString(new String[]{steArray[3].getMethodName(),
                            String.valueOf(steArray[3].getLineNumber())}));
        } else {
            // Legitimate
            Log.w(classSimpleName,
                    Arrays.toString(new String[]{steArray[index].getMethodName(),
                            String.valueOf(steArray[index].getLineNumber())}));
        }
    }

    public AlertDialog showMessage(Context context, String title, String message,
                                   String positiveButton) {
        return new AlertDialog.Builder(context, R.style.AppAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, null)
                .show();
    }

    public AlertDialog showMessage(Context context, String title, String message,
                                   String positiveButton,
                                   DialogInterface.OnClickListener positiveButtonListener,
                                   String negativeButton,
                                   DialogInterface.OnClickListener negativeButtonListener) {
        return new AlertDialog.Builder(context, R.style.AppAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, positiveButtonListener)
                .setNegativeButton(negativeButton, negativeButtonListener)
                .setCancelable(false)
                .show();
    }

    public boolean isGPSLocationEnabled(final Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private AlertDialog showLocationMessage(final Context context) {
        return showMessage(context, "Location off!", "Do you want to turn on Location?", "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }, "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(1);
                    }
                });

    }

    public AlertDialog checkLocationSettings(final Context context) {
        if (isGPSLocationEnabled(context)) {
            LocationService.start(context);
        } else {
            return showLocationMessage(context);
        }
        return null;
    }

    public AlertDialog checkLocationSettings(
            final Context context, final LocationService.LocationListener locationListener) {
        if (isGPSLocationEnabled(context)) {
            LocationService.start(context, locationListener);
        } else {
            return showLocationMessage(context);
        }
        return null;
    }

    public AlertDialog checkLocationSettings(
            final Context context, final LocationService.LocationListener locationListener,
            final boolean locationMessage) {
        if (isGPSLocationEnabled(context)) {
            LocationService.start(context, locationListener);
        } else if (locationMessage) {
            return showLocationMessage(context);
        }
        return null;
    }

    /**
     * @param o Object returned by callMethod()
     */
    public boolean isOdooResponseSuccessful(Object o) {
        try {
            if (new JSONObject(o.toString()).optJSONArray("result") != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONArray getOdooResponseResult(@NonNull Object o) throws JSONException {
        return new JSONObject(o.toString())
                .getJSONArray("result");
    }

    public JSONObject getOdooResponseError(@NonNull Object o) throws JSONException {
        return new JSONObject(o.toString())
                .getJSONObject("error");
    }
}
