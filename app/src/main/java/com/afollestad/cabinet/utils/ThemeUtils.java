package com.afollestad.cabinet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.afollestad.cabinet.R;
import com.afollestad.cabinet.fragments.ColorChooserDialog;
import com.afollestad.materialdialogs.Theme;

public class ThemeUtils {

    public ThemeUtils(Activity context) {
        mContext = context;
        isChanged(); // invalidate stored booleans
    }

    private Context mContext;
    private boolean mDarkMode;
    private boolean mTrueBlack;
    private int mLastPrimaryColor;
    private int mLastAccentColor;
    private boolean mLastColoredNav;

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("dark_mode", false);
    }

    public static boolean isTrueBlack(Context context) {
        if (!isDarkMode(context)) return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("true_black", false);
    }

    public int primaryColor() {
        String key = "primary_color";
        if (mDarkMode || mTrueBlack) key += "_dark";
        else key += "_light";
        final int defaultColor = mContext.getResources().getColor(R.color.cabinet_color);
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(key, defaultColor);
    }

    public void primaryColor(int newColor) {
        String key = "primary_color";
        if (mDarkMode || mTrueBlack) key += "_dark";
        else key += "_light";
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(key, newColor).commit();
    }

    public int primaryColorDark() {
        return ColorChooserDialog.shiftColorDown(primaryColor());
    }

    public int accentColor() {
        String key = "accent_color";
        if (mDarkMode || mTrueBlack) key += "_dark";
        else key += "_light";
        final int defaultColor = mContext.getResources().getColor(R.color.cabinet_accent_color);
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(key, defaultColor);
    }

    public int accentColorLight() {
        return ColorChooserDialog.shiftColorUp(accentColor());
    }

    public int accentColorDark() {
        return ColorChooserDialog.shiftColorDown(accentColor());
    }

    public void accentColor(int newColor) {
        String key = "accent_color";
        if (mDarkMode || mTrueBlack) key += "_dark";
        else key += "_light";
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(key, newColor).commit();
    }

    public boolean isColoredNavBar() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("colored_navbar", true);
    }

    public static Theme getDialogTheme(Context context) {
        if (isDarkMode(context) || isTrueBlack(context)) return Theme.DARK;
        else return Theme.LIGHT;
    }

    public boolean isChanged() {
        final boolean darkTheme = isDarkMode(mContext);
        final boolean blackTheme = isTrueBlack(mContext);
        final int primaryColor = primaryColor();
        final int accentColor = accentColor();
        final boolean coloredNav = isColoredNavBar();

        final boolean changed = mDarkMode != darkTheme || mTrueBlack != blackTheme ||
                mLastPrimaryColor != primaryColor || mLastAccentColor != accentColor ||
                coloredNav != mLastColoredNav;
        mDarkMode = darkTheme;
        mTrueBlack = blackTheme;
        mLastPrimaryColor = primaryColor;
        mLastAccentColor = accentColor;
        mLastColoredNav = coloredNav;
        return changed;
    }

    public int getCurrent(boolean hasNavDrawer) {
        if (hasNavDrawer) {
            if (mTrueBlack) {
                return R.style.Theme_CabinetTrueBlack_WithNavDrawer;
            } else if (mDarkMode) {
                return R.style.Theme_CabinetDark_WithNavDrawer;
            } else {
                return R.style.Theme_Cabinet_WithNavDrawer;
            }
        } else {
            if (mTrueBlack) {
                return R.style.Theme_CabinetTrueBlack;
            } else if (mDarkMode) {
                return R.style.Theme_CabinetDark;
            } else {
                return R.style.Theme_Cabinet;
            }
        }
    }
}
