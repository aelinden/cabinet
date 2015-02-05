package com.afollestad.cabinet.file.root;

import android.app.Activity;

import com.afollestad.cabinet.file.base.File;
import com.afollestad.cabinet.file.base.FileFilter;

import java.util.ArrayList;
import java.util.List;

public class LsParser {

    private Activity mContext;
    private String mPath;
    private FileFilter mFilter;
    private List<File> mFiles;
    public RootFile file;
    private boolean includeHidden;

    private LsParser() {
        mFiles = new ArrayList<>();
    }

    public static LsParser parse(Activity context, String path, List<String> response, FileFilter filter, boolean includeHidden) {
        if (path.equals("/")) path = "";
        LsParser parser = new LsParser();
        parser.mContext = context;
        parser.mPath = path;
        parser.mFilter = filter;
        parser.includeHidden = includeHidden;
        for (String line : response) {
            parser.processLine(line);
        }
        return parser;
    }

    protected void processLine(String line) {
        file = new RootFile(mContext);
        final String[] split = line.split(" ");
        int index = 0;

        for (String token : split) {
            if (token.trim().isEmpty())
                continue;
            switch (index) {
                case 0: {
                    file.permissions = token;
                    break;
                }
                case 1: {
                    file.owner = token;
                    break;
                }
                case 2: {
                    file.creator = token;
                    break;
                }
                case 3: {
                    if (token.contains("-")) {
                        // No length, this is the date
                        file.size = -1;
                        file.date = token;
                    } else {
                        // Length, this is a file
                        file.size = Long.parseLong(token);
                    }
                    break;
                }
                case 4: {
                    if (file.size == -1) {
                        // This is the time
                        file.time = token;
                    } else {
                        // This is the date
                        file.date = token;
                    }
                    break;
                }
                case 5: {
                    if (file.size > -1) {
                        // This is the time
                        file.time = token;
                    }
                    break;
                }
            }
            index++;
        }

        final String nameAndLink = line.substring(
                line.indexOf(file.time) + file.time.length() + 1);
        if (nameAndLink.contains(" -> ")) {
            final String[] splitSl = nameAndLink.split(" -> ");
            file.originalName = splitSl[0];
            file.setPath(splitSl[1]);
        } else {
            file.originalName = nameAndLink;
            file.setPath(mPath + "/" + nameAndLink);
        }

        boolean skip = includeHidden && file.getName().startsWith(".");
        if ((mFilter == null || mFilter.accept(file)) && !skip)
            mFiles.add(file);
    }

    public List<File> getFiles() {
        return mFiles;
    }
}