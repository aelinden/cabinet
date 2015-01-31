package com.afollestad.cabinet.ui.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.afollestad.cabinet.utils.ThemeUtils;
import com.afollestad.materialdialogs.ThemeSingleton;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class ThemableActivity extends ActionBarActivity {

    private ThemeUtils mThemeUtils;

    protected boolean hasNavDrawer() {
        return false;
    }

    public ThemeUtils getThemeUtils() {
        return mThemeUtils;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeUtils = new ThemeUtils(this);
        setTheme(mThemeUtils.getCurrent(hasNavDrawer()));
        super.onCreate(savedInstanceState);

        final int accent = mThemeUtils.accentColor();
        ThemeSingleton.get().positiveColor = accent;
        ThemeSingleton.get().neutralColor = accent;
        ThemeSingleton.get().negativeColor = accent;
        ThemeSingleton.get().darkTheme = ThemeUtils.isDarkMode(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int dark = getThemeUtils().primaryColorDark();
            getWindow().setStatusBarColor(dark);
            if (getThemeUtils().isColoredNavBar())
                getWindow().setNavigationBarColor(dark);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mThemeUtils.isChanged(true)) {
            setTheme(mThemeUtils.getCurrent(hasNavDrawer()));
            recreate();
        }
    }
}
