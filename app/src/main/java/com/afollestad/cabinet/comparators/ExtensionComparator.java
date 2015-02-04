package com.afollestad.cabinet.comparators;

import android.content.Context;
import android.preference.PreferenceManager;

import com.afollestad.cabinet.file.base.File;

/**
 * Sorts files by extension, alphabetically. Folders will be at the beginning.
 *
 * @author Aidan Follestad (afollestad)
 */
public class ExtensionComparator implements java.util.Comparator<File> {

    private boolean foldersFirst;

    public ExtensionComparator(Context context) {
        foldersFirst = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("folders_first", true);
    }

    @Override
    public int compare(File lhs, File rhs) {
        if (foldersFirst) {
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            } else if (lhs.isDirectory() == rhs.isDirectory()) {
                // Sort my extension once sorted by folders
                final int extensionCompare = lhs.getExtension().compareTo(rhs.getExtension());
                if (extensionCompare == 0) {
                    // If the extensions are the same, sort by name
                    return lhs.getName().compareTo(rhs.getName());
                }
                return extensionCompare;
            } else {
                return 1;
            }
        } else {
            final int extensionCompare = lhs.getExtension().compareTo(rhs.getExtension());
            if (extensionCompare == 0) {
                // If the extensions are the same, sort by name
                return lhs.getName().compareTo(rhs.getName());
            }
            return extensionCompare;
        }
    }
}