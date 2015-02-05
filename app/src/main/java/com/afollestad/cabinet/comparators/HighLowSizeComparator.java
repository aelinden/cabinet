package com.afollestad.cabinet.comparators;

import android.content.Context;
import android.preference.PreferenceManager;

import com.afollestad.cabinet.file.base.File;

/**
 * Sorts files and folders by size, from large to small. Folders are considered large.
 *
 * @author Aidan Follestad (afollestad)
 */
public class HighLowSizeComparator implements java.util.Comparator<File> {

    private boolean foldersFirst;

    public HighLowSizeComparator(Context context) {
        if(context != null)
            foldersFirst = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("folders_first", true);
    }

    @Override
    public int compare(File lhs, File rhs) {
        if (foldersFirst) {
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            } else if (lhs.isDirectory() == rhs.isDirectory()) {
                // Sort by size once sorted by folders
                return Long.valueOf(rhs.length()).compareTo(lhs.length());
            } else {
                return 1;
            }
        } else {
            return Long.valueOf(rhs.length()).compareTo(lhs.length());
        }
    }
}