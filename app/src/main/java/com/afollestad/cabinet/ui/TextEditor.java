package com.afollestad.cabinet.ui;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.cabinet.R;
import com.afollestad.cabinet.file.LocalFile;
import com.afollestad.cabinet.file.base.File;
import com.afollestad.cabinet.file.root.RootFile;
import com.afollestad.cabinet.fragments.DetailsDialog;
import com.afollestad.cabinet.sftp.SftpClient;
import com.afollestad.cabinet.ui.base.NetworkedActivity;
import com.afollestad.cabinet.utils.Utils;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends NetworkedActivity implements TextWatcher {

    private final static int MODIFIED_CHECK_INTERVAL = 150;

    private EditText mInput;
    private java.io.File mFile;
    private java.io.File mTempFile;
    private String mOriginal;
    private Timer mTimer;
    private boolean mModified;

    private int mFindStart = 0;
    private EditText mFindText;
    private EditText mReplaceText;

    private String trim(String text) {
        if (text.length() == 0) return text;
        else if (text.charAt(0) != '\n' && text.charAt(0) != ' ' &&
                text.charAt(text.length() - 1) != '\n' &&
                text.charAt(text.length() - 1) != ' ') {
            return text;
        }
        final StringBuilder b = new StringBuilder(text);
        while (true) {
            if (b.length() == 0) break;
            else if (b.charAt(0) == '\n' || b.charAt(0) == ' ') {
                b.deleteCharAt(0);
            } else {
                break;
            }
        }
        while (true) {
            if (b.length() == 0) break;
            else if (b.charAt(b.length() - 1) == '\n' || b.charAt(b.length() - 1) == ' ') {
                b.deleteCharAt(b.length() - 1);
            } else {
                break;
            }
        }
        return b.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());

        mInput = (EditText) findViewById(R.id.input);
        mInput.addTextChangedListener(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getData() != null) load(getIntent().getData());
        else mInput.setVisibility(View.VISIBLE);

        findViewById(R.id.findReplaceFrame).setBackgroundColor(getThemeUtils().primaryColor());

        TextView find = (TextView) findViewById(R.id.btnFind);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFind(true);
            }
        });

        TextView replace = (TextView) findViewById(R.id.btnReplace);
        replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performReplace();
            }
        });

        TextView replaceAll = (TextView) findViewById(R.id.btnReplaceAll);
        replaceAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (performReplace()) {
                    // Do nothing, it will stop on its own
                }
            }
        });
    }

    private boolean performFind(boolean showErrorIfNone) {
        String findText = trim(mFindText.getText().toString());
        if (findText.length() == 0) return false;
        String mainText = trim(mInput.getText().toString());
        final boolean matchCase = ((CheckBox) findViewById(R.id.match_case)).isChecked();

        if (!matchCase) {
            findText = findText.toLowerCase(Locale.getDefault());
            mainText = mainText.toLowerCase(Locale.getDefault());
        }

        final Pattern p = Pattern.compile(findText);
        final Matcher m = p.matcher(mainText);
        if (mFindStart > mainText.length() - 1)
            mFindStart = 0;
        boolean found = m.find(mFindStart);
        if (!found && mFindStart > 0) {
            mFindStart = 0;
            found = m.find(mFindStart);
        }
        if (!found) {
            if (showErrorIfNone) {
                new MaterialDialog.Builder(this)
                        .title(R.string.no_occurences)
                        .content(R.string.no_occurences_desc)
                        .positiveText(android.R.string.ok)
                        .show();
            }
            return false;
        }

        mFindStart = m.start();
        int mFindEnd = m.end();
        mInput.requestFocus();
        mInput.setSelection(mFindStart, mFindEnd);
        mFindStart = mFindEnd + 1;
        return true;
    }

    private boolean performReplace() {
        int mLastReplaceStart = -1;
        if (mInput.getSelectionStart() >= 0 && mInput.getSelectionEnd() > mInput.getSelectionStart() &&
                mLastReplaceStart != mInput.getSelectionStart()) {
            final int start = mInput.getSelectionStart();
            final int end = mInput.getSelectionEnd();
            final StringBuilder inputText = new StringBuilder(mInput.getText().toString());
            inputText.delete(start, end);
            inputText.insert(start, mReplaceText.getText().toString());
            mInput.setText(inputText.toString());
            return performFind(false);
        } else return performFind(true) && performReplace();
    }

    @Override
    protected boolean disconnectOnNotify() {
        return false;
    }

    private void setProgress(boolean show) {
        mInput.setVisibility(show ? View.GONE : View.VISIBLE);
        findViewById(R.id.progress).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void load(final Uri uri) {
        setProgress(true);
        Log.v("TextEditor", "Loading...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mFile = new java.io.File(uri.getPath());
                if (!mFile.exists()) {
                    Log.v("TextEditor", "File doesn't exist...");
                    finish();
                    return;
                }

                if (new LocalFile(TextEditor.this, mFile).requiresRoot()) {
                    new RootFile(TextEditor.this, mFile).mountParent(true);
                }

                String ext = File.getExtension(mFile.getName()).toLowerCase(Locale.getDefault());
                String mime = File.getMimeType(TextEditor.this, ext);
                Log.v("TextEditor", "Mime: " + mime);
                List<String> textExts = Arrays.asList(getResources().getStringArray(R.array.other_text_extensions));
                List<String> codeExts = Arrays.asList(getResources().getStringArray(R.array.code_extensions));
                if (!mime.startsWith("text/") && !textExts.contains(ext) && !codeExts.contains(ext)) {
                    Log.v("TextEditor", "Unsupported extension");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MaterialDialog.Builder(TextEditor.this)
                                    .title(R.string.unsupported_extension)
                                    .content(R.string.unsupported_extension_desc)
                                    .positiveText(android.R.string.ok)
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            finish();
                                        }
                                    }).build().show();
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitle(mFile.getName());
                    }
                });
                Log.v("TextEditor", "Reading file...");
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mFile), "UTF-8"));
                    String line;
                    final StringBuilder text = new StringBuilder();
                    try {
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                    } catch (final OutOfMemoryError e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showErrorDialog(TextEditor.this, e.getLocalizedMessage());
                            }
                        });
                    }
                    br.close();
                    Log.v("TextEditor", "Setting contents to input area...");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mOriginal = text.toString();
                                text.setLength(0); // clear string builder to reduce memory usage
                                mInput.setText(mOriginal);
                            } catch (OutOfMemoryError e) {
                                Utils.showErrorDialog(TextEditor.this, e.getLocalizedMessage());
                            }
                            setProgress(false);
                        }
                    });
                } catch (final Exception e) {
                    Log.v("TextEditor", "Error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showErrorDialog(TextEditor.this, e.getLocalizedMessage());
                            setProgress(false);
                        }
                    });
                }
            }
        }).start();
    }

    private void upload(final boolean exitAfter) {
        if (getRemoteSwitch() == null) {
            if (exitAfter) finish();
            return;
        }
        Log.v("TextEditor", "Uploading changes to remote server...");
        new LocalFile(this, mFile).copy(getRemoteSwitch(), new SftpClient.FileCallback() {
            @Override
            public void onComplete(File file) {
                if (exitAfter) finish();
            }

            @Override
            public void onError(Exception e) {
                // Dialog is already shown, can ignore this
            }
        }, false);
    }

    private void save(final boolean exitAfter) {
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage(getString(R.string.saving));
        mDialog.setCancelable(false);
        mDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (new LocalFile(TextEditor.this, mFile).requiresRoot()) {
                        new RootFile(TextEditor.this, mFile).mountParent(true);
                        mTempFile = java.io.File.createTempFile(mFile.getName(), ".temp");
                    }
                    Log.v("TextEditor", "Writing changes to " + (mTempFile != null ?
                            mTempFile.getPath() : mFile.getPath()) + "...");

                    FileOutputStream os = new FileOutputStream(mTempFile != null ? mTempFile : mFile);
                    os.write(mInput.getText().toString().getBytes("UTF-8"));
                    os.close();

                    if (mTempFile != null) {
                        try {
                            Log.v("TextEditor", "Moving temporary file " + mTempFile.getPath() + " to " + mFile.getPath() + "...");
                            RootFile.runAsRoot(TextEditor.this, "mv -f \"" + mTempFile.getAbsolutePath() + "\" \"" + mFile.getAbsolutePath() + "\"",
                                    new RootFile(TextEditor.this, mFile.getParentFile()));
                        } catch (final Exception e) {
                            throw e;
                        } finally {
                            Log.v("TextEditor", "Deleting temp file " + mTempFile.getPath() + "...");
                            mTempFile.delete();
                            mTempFile = null;
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.dismiss();
                            Log.v("TextEditor", "Save complete!");
                            if (exitAfter) {
                                mOriginal = null;
                                upload(exitAfter);
                            } else {
                                mOriginal = mInput.getText().toString();
                                mModified = false;
                                invalidateOptionsMenu();
                                upload(exitAfter);
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showErrorDialog(TextEditor.this, e.getLocalizedMessage());
                            mDialog.dismiss();
                        }
                    });
                } finally {
                    if (new LocalFile(TextEditor.this, mFile).requiresRoot()) {
                        new RootFile(TextEditor.this, mFile).unmountParent(true);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        checkUnsavedChanges();
    }

    private void checkUnsavedChanges() {
        if (mOriginal != null && !mOriginal.equals(mInput.getText().toString())) {
            new MaterialDialog.Builder(this)
                    .title(R.string.unsaved_changes)
                    .content(R.string.unsaved_changes_desc)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            save(true);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            finish();
                        }
                    })
                    .build().show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_editor, menu);
        menu.findItem(R.id.save).setVisible(mModified);
        menu.findItem(R.id.revert).setVisible(mModified);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            checkUnsavedChanges();
            return true;
        } else if (item.getItemId() == R.id.save) {
            save(false);
            return true;
        } else if (item.getItemId() == R.id.details) {
            DetailsDialog.create(new LocalFile(this, mFile)).show(getFragmentManager(), "DETAILS_DIALOG");
            return true;
        } else if (item.getItemId() == R.id.revert) {
            mModified = false;
            mInput.setText(mOriginal);
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.findAndReplace) {
            View frame = findViewById(R.id.findReplaceFrame);
            if (frame.getVisibility() == View.GONE) {
                frame.setVisibility(View.VISIBLE);
                mFindText = (EditText) findViewById(R.id.find);
                mReplaceText = (EditText) findViewById(R.id.replace);
                mFindText.requestFocus();
            } else {
                frame.setVisibility(View.GONE);
                mInput.requestFocus();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mModified = !mInput.getText().toString().equals(mOriginal);
                invalidateOptionsMenu();
            }
        }, MODIFIED_CHECK_INTERVAL);
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}