package com.afollestad.cabinet.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;

public class StorageHelper {

    Context mContext;
    BroadcastReceiver mExternalStorageReceiver;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    StateListener mListener;

    public StorageHelper(Context context, StateListener listener) {
        mContext = context;
        mListener = listener;
    }

    private void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_MOUNTED:
                mExternalStorageAvailable = mExternalStorageWriteable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
                break;
            default:
                mExternalStorageAvailable = mExternalStorageWriteable = false;
                break;
        }
        log("State = " + state + "; available = " + mExternalStorageAvailable + "; writeable = " + mExternalStorageWriteable);
        if (mListener != null)
            mListener.onStateChanged(mExternalStorageAvailable, mExternalStorageWriteable);
    }

    public void startWatchingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                log("Storage: " + intent.getData());
                updateExternalStorageState();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        mContext.registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();
    }

    public void stopWatchingExternalStorage() {
        mContext.unregisterReceiver(mExternalStorageReceiver);
    }

    private void log(String message) {
        Log.v("StorageHelper", message);
    }

    public interface StateListener {
        void onStateChanged(boolean available, boolean writeable);
    }
}
