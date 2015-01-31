package com.afollestad.cabinet.ui;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.cabinet.R;
import com.afollestad.cabinet.fragments.AboutDialog;
import com.afollestad.cabinet.fragments.ColorChooserDialog;
import com.afollestad.cabinet.ui.base.ThemableActivity;
import com.afollestad.cabinet.utils.ThemeUtils;
import com.afollestad.cabinet.utils.Utils;
import com.afollestad.cabinet.views.CabinetPreference;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author Aidan Follestad (afollestad)
 */
public class SettingsActivity extends ThemableActivity
        implements AboutDialog.DismissListener, ColorChooserDialog.ColorCallback {

    private static boolean aboutDialogShown;

    @Override
    public void onColorSelection(int title, int color) {
        if (title == R.string.primary_color)
            getThemeUtils().primaryColor(color);
        else
            getThemeUtils().accentColor(color);
        recreate();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            findPreference("dark_mode").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    ImageLoader.getInstance().clearMemoryCache();
                    getActivity().recreate();
                    return true;
                }
            });
            findPreference("true_black").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    getActivity().recreate();
                    return true;
                }
            });

            findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (aboutDialogShown) return false;
                    aboutDialogShown = true; // double clicking without this causes the dialog to be shown twice
                    new AboutDialog().show(getFragmentManager(), "ABOUT");
                    return true;
                }
            });

            Preference coloredNav = findPreference("colored_navbar");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                coloredNav.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (getActivity() != null)
                            getActivity().recreate();
                        return true;
                    }
                });
            } else {
                coloredNav.setEnabled(false);
                coloredNav.setSummary(R.string.only_available_lollipop);
            }


            ThemeUtils themeUtils = ((ThemableActivity) getActivity()).getThemeUtils();
            CabinetPreference primaryColor = (CabinetPreference) findPreference("primary_color");
            primaryColor.setColor(themeUtils.primaryColor(), Utils.resolveColor(getActivity(), R.attr.colorAccent));
            primaryColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ThemeUtils themeUtils = ((ThemableActivity) getActivity()).getThemeUtils();
                    new ColorChooserDialog().show(getActivity(), preference.getTitleRes(),
                            themeUtils.primaryColor());
                    return true;
                }
            });


            CabinetPreference accentColor = (CabinetPreference) findPreference("accent_color");
            accentColor.setColor(themeUtils.accentColor(), Utils.resolveColor(getActivity(), R.attr.colorAccent));
            accentColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ThemeUtils themeUtils = ((ThemableActivity) getActivity()).getThemeUtils();
                    new ColorChooserDialog().show(getActivity(), preference.getTitleRes(),
                            themeUtils.accentColor());
                    return true;
                }
            });
        }
    }

    @Override
    public void onDismiss() {
        aboutDialogShown = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_activity_custom);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.settings_content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}