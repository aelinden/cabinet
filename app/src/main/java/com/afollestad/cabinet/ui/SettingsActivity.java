package com.afollestad.cabinet.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.cabinet.R;
import com.afollestad.cabinet.fragments.AboutDialog;
import com.afollestad.cabinet.fragments.ColorChooserDialog;
import com.afollestad.cabinet.ui.base.ThemableActivity;
import com.afollestad.cabinet.utils.ThemeUtils;
import com.afollestad.cabinet.utils.Utils;
import com.afollestad.cabinet.views.CabinetPreference;
import com.afollestad.materialdialogs.MaterialDialog;
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
        else if (title == R.string.accent_color)
            getThemeUtils().accentColor(color);
        else
            getThemeUtils().thumbnailColor(color);
        recreate();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            findPreference("base_theme").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ImageLoader.getInstance().clearMemoryCache();

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    int preselect = 0;
                    if (prefs.getBoolean("true_black", false)) {
                        preselect = 2;
                    } else if (prefs.getBoolean("dark_mode", false)) {
                        preselect = 1;
                    }

                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.base_theme)
                            .items(R.array.base_themes)
                            .itemsCallbackSingleChoice(preselect, new MaterialDialog.ListCallback() {
                                @SuppressLint("CommitPrefEdits")
                                @Override
                                public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                    if (getActivity() == null) return;
                                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                    switch (i) {
                                        default:
                                            prefs.remove("dark_mode").remove("true_black");
                                            break;
                                        case 1:
                                            prefs.remove("true_black")
                                                    .putBoolean("dark_mode", true);
                                            break;
                                        case 2:
                                            prefs.putBoolean("dark_mode", true)
                                                    .putBoolean("true_black", true);
                                            break;
                                    }
                                    prefs.commit();
                                    ImageLoader.getInstance().clearMemoryCache();
                                    getActivity().recreate();
                                }
                            }).show();
                    return false;
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

            CabinetPreference thumbnailColor = (CabinetPreference) findPreference("thumbnail_color");
            thumbnailColor.setColor(themeUtils.thumbnailColor(), Utils.resolveColor(getActivity(), R.attr.colorAccent));
            thumbnailColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ThemeUtils themeUtils = ((ThemableActivity) getActivity()).getThemeUtils();
                    new ColorChooserDialog().show(getActivity(), preference.getTitleRes(),
                            themeUtils.thumbnailColor());
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