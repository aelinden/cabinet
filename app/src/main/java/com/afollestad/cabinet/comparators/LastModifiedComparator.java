package com.afollestad.cabinet.comparators;

import android.content.Context;
import android.preference.PreferenceManager;

import com.afollestad.cabinet.file.base.File;

/**
 * @author Aidan Follestad (afollestad)
 */
public class LastModifiedComparator implements java.util.Comparator<File> {

    private boolean foldersFirst;

    public LastModifiedComparator(Context context) {
        foldersFirst = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("folders_first", true);
    }

    @Override
    public int compare(File lhs, File rhs) {
        if (foldersFirst) {
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            } else if (lhs.isDirectory() == rhs.isDirectory()) {
                // Sort by modified date once sorted by folders
                return Long.valueOf(rhs.lastModified()).compareTo(lhs.lastModified());
            } else {
                return 1;
            }
        } else {
            return Long.valueOf(rhs.lastModified()).compareTo(lhs.lastModified());
        }
    }
}