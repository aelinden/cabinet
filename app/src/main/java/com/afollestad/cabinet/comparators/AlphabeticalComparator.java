package com.afollestad.cabinet.comparators;

import android.content.Context;
import android.preference.PreferenceManager;

import com.afollestad.cabinet.file.base.File;

/**
 * Sorts files and folders by name, alphabetically.
 *
 * @author Aidan Follestad (afollestad)
 */
public class AlphabeticalComparator implements java.util.Comparator<File> {

    private boolean foldersFirst;

    public AlphabeticalComparator(Context context) {
        if (context != null)
            foldersFirst = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("folders_first", true);
    }

    @Override
    public int compare(File lhs, File rhs) {
        if (foldersFirst) {
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            } else if (lhs.isDirectory() == rhs.isDirectory()) {
                // Sort my name once sorted by folders
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            } else {
                return 1;
            }
        } else {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}