package com.afollestad.cabinet.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.cabinet.R;
import com.afollestad.cabinet.adapters.NavigationDrawerAdapter;
import com.afollestad.cabinet.file.base.File;
import com.afollestad.cabinet.ui.DrawerActivity;
import com.afollestad.cabinet.utils.Pins;
import com.afollestad.cabinet.utils.StorageHelper;
import com.afollestad.cabinet.utils.Utils;

public class NavigationDrawerFragment extends Fragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String STATE_TITLE = "title";

    private DrawerLayout mDrawerLayout;
    private RecyclerView mRecyclerView;
    private NavigationDrawerAdapter mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    private int mCurrentSelectedPosition = 1;
    private CharSequence mTitle;

    private StorageHelper mStorageHelper;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mTitle = savedInstanceState.getCharSequence(STATE_TITLE);
        }
        mStorageHelper = new StorageHelper(getActivity(), new StorageHelper.StateListener() {
            @Override
            public void onStateChanged(boolean available, boolean writeable) {
                // TODO?
            }
        });
        mStorageHelper.startWatchingExternalStorage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStorageHelper.stopWatchingExternalStorage();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawer, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        mRecyclerView.setClipToPadding(false);
        mAdapter = new NavigationDrawerAdapter(getActivity(), new NavigationDrawerAdapter.ClickListener() {
            @Override
            public void onClick(int index) {
                selectItem(index);
            }

            @Override
            public boolean onLongClick(final int index) {
                Pins.Item item = mAdapter.getItem(index);
                Utils.showConfirmDialog(getActivity(), R.string.remove_shortcut,
                        R.string.confirm_remove_shortcut, item.getDisplay(getActivity()), new Utils.ClickListener() {
                            @Override
                            public void onPositive(int which, View view) {
                                Pins.remove(getActivity(), index);
                                mAdapter.reload(getActivity());
                            }
                        }
                );
                return false;
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setCheckedPos(mCurrentSelectedPosition);
        return v;
    }

    public void setUp(DrawerLayout drawerLayout, Toolbar actionBarToolbar) {
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, actionBarToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Don't call super method to disable the rotating nav icon
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        if (position < 0) position = 1;
        mCurrentSelectedPosition = position;
        if (mRecyclerView != null) {
            mAdapter.setCheckedPos(position);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
        DrawerActivity act = (DrawerActivity) getActivity();
        Pins.Item item = mAdapter.getItem(position);
        act.switchDirectory(item);
        mTitle = item.getDisplay(getActivity());
        mDrawerLayout.closeDrawers();
    }

    public void selectFile(File file) {
        mCurrentSelectedPosition = mAdapter.setCheckedFile(file);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putCharSequence(STATE_TITLE, mTitle);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public void reload(boolean open) {
        Activity act = getActivity();
        if (act != null) {
            mAdapter.reload(act);
            if (open) mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    public void invalidatePadding(boolean cabStarted) {
        if (getView() == null || getActivity() == null) return;
        final View v = getView();
        if (cabStarted) {
            int actionBarHeight;
            TypedValue tv = new TypedValue();
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            else
                actionBarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getResources().getDisplayMetrics());
            v.setPadding(v.getPaddingLeft(), actionBarHeight, v.getPaddingRight(), v.getPaddingBottom());
        } else {
            v.setPadding(v.getPaddingLeft(), 0, v.getPaddingRight(), v.getPaddingBottom());
        }
    }
}