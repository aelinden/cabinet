package com.afollestad.cabinet.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.cabinet.R;
import com.afollestad.cabinet.file.LocalFile;
import com.afollestad.cabinet.file.base.File;
import com.afollestad.cabinet.utils.Pins;
import com.afollestad.cabinet.utils.Utils;
import com.afollestad.materialdialogs.ThemeSingleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ShortcutViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    @Override
    public void onClick(View view) {
        int index = (Integer) view.getTag();
        if (index == mItems.size() + 1)
            mListener.onClickSettings();
        else if (index == mItems.size())
            mListener.onClickDonate();
        else
            mListener.onClick(index);
    }

    @Override
    public boolean onLongClick(View view) {
        mListener.onLongClick((Integer) view.getTag());
        return false;
    }

    public interface ClickListener {
        public abstract void onClick(int index);

        public abstract boolean onLongClick(int index);

        public abstract void onClickSettings();

        public abstract void onClickDonate();
    }

    public NavigationDrawerAdapter(Activity context, ClickListener listener) {
        mContext = context;
        mItems = new ArrayList<>();
        mListener = listener;
        bodyText = Utils.resolveColor(context, R.attr.body_text);

        if (Pins.getAll(context).size() == 0) {
            LocalFile item = new LocalFile(context);
            Pins.add(context, new Pins.Item(item));
            item = new LocalFile(context, Environment.getExternalStorageDirectory());
            Pins.add(context, new Pins.Item(item));
            try {

                // TODO SD card stuff
//                java.io.File sd = new java.io.File("/external_sd");
//                if (sd.exists()) {
//                    item = new LocalFile(context, sd);
//                    Pins.add(context, new Pins.Item(item));
//                } else {
//                    sd = new java.io.File("/extSdCard");
//                    if (sd.exists()) {
//                        item = new LocalFile(context, sd);
//                        Pins.add(context, new Pins.Item(item));
//                    }
//                }

                item = new LocalFile(context, new java.io.File(Environment.getExternalStorageDirectory(), "DCIM"));
                if (item.existsSync())
                    Pins.add(context, new Pins.Item(item));
                item = new LocalFile(context, new java.io.File(Environment.getExternalStorageDirectory(), "Download"));
                if (item.existsSync())
                    Pins.add(context, new Pins.Item(item));
                item = new LocalFile(context, new java.io.File(Environment.getExternalStorageDirectory(), "Music"));
                if (item.existsSync())
                    Pins.add(context, new Pins.Item(item));
                item = new LocalFile(context, new java.io.File(Environment.getExternalStorageDirectory(), "Pictures"));
                if (item.existsSync())
                    Pins.add(context, new Pins.Item(item));
            } catch (Exception e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        reload(context);
    }

    private Activity mContext;
    private List<Pins.Item> mItems;
    private int mCheckedPos = -1;
    private ClickListener mListener;
    private int bodyText;

    public void reload(Context context) {
        final List<Pins.Item> items = Pins.getAll(context);
        mItems.clear();
        for (Pins.Item i : items)
            mItems.add(i);
        notifyDataSetChanged();
    }

    public int setCheckedFile(File file) {
        int index = -1;
        for (int i = 0; i < mItems.size(); i++) {
            Pins.Item item = mItems.get(i);
            if (item.getPath() != null && item.getPath().equals(file.getPath())) {
                index = i;
                break;
            }
        }
        setCheckedPos(index);
        return index;
    }

    public void setCheckedPos(int index) {
        int beforeChecked = mCheckedPos;
        mCheckedPos = index;
        if (beforeChecked > -1)
            notifyItemChanged(beforeChecked);
        notifyItemChanged(mCheckedPos);
    }

    public Pins.Item getItem(int index) {
        if (mItems.size() == 0) return null;
        return mItems.get(index);
    }

    public static class ShortcutViewHolder extends RecyclerView.ViewHolder {

        public ShortcutViewHolder(View itemView) {
            super(itemView);

            divider = itemView.findViewById(R.id.divider);

            item = itemView.findViewById(R.id.item);
            title = (TextView) itemView.findViewById(R.id.title);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }

        TextView title;
        ImageView icon;
        View divider;
        View item;
    }

    @Override
    public ShortcutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_drawer, parent, false);
        return new ShortcutViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ShortcutViewHolder holder, int index) {
        holder.item.setTag(index);
        holder.item.setOnClickListener(this);

        final int currentColor = mCheckedPos == index ? ThemeSingleton.get().positiveColor : bodyText;
        holder.icon.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP);
        holder.divider.setVisibility(View.GONE);

        if (index == mItems.size() || index == mItems.size() + 1) {
            if (index == mItems.size() + 1) {
                holder.title.setText(R.string.settings);
                holder.icon.setImageResource(R.drawable.ic_drawer_settings);
            } else {
                holder.title.setText(R.string.donate);
                holder.icon.setImageResource(R.drawable.ic_drawer_donate);
                holder.divider.setVisibility(View.VISIBLE);
            }
            holder.title.setTextColor(bodyText);
        } else {
            Pins.Item item = mItems.get(index);
            holder.item.setOnLongClickListener(this);
            holder.item.setActivated(mCheckedPos == index);
            holder.title.setTextColor(currentColor);

            if (item.isRemote()) {
                holder.title.setText(item.getDisplay(mContext));
            } else {
                File file = new LocalFile(mContext, item.getPath());
                if (file.isRoot()) {
                    holder.title.setText(R.string.root);
                } else if (file.isStorageDirectory()) {
                    holder.title.setText(R.string.storage);
                } else if (file.getName().startsWith("sdcard")) {
                    holder.title.setText(R.string.sdcard);
                } else {
                    holder.title.setText(item.getDisplay(mContext));
                }
            }

            loadThumbnail(item, holder.icon);
        }
    }

    private void loadThumbnail(Pins.Item item, ImageView icon) {
        final String p = item.getPath().toLowerCase(Locale.getDefault());
        if (p.equals("/")) {
            icon.setImageResource(R.drawable.ic_drawer_root);
        } else if (p.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            icon.setImageResource(R.drawable.ic_drawer_storage);
        } else if (p.contains("dcim") || p.contains("camera") ||
                p.contains("video") || p.contains("movie")) {
            icon.setImageResource(R.drawable.ic_drawer_camera);
        } else if (p.contains("download")) {
            icon.setImageResource(R.drawable.ic_drawer_download);
        } else if (p.contains("music") || p.contains("audio") ||
                p.contains("ringtone") || p.contains("notification") ||
                p.contains("podcast") || p.contains("alarm")) {
            icon.setImageResource(R.drawable.ic_drawer_audio);
        } else if (p.contains("picture") || p.contains("instagram")) {
            icon.setImageResource(R.drawable.ic_drawer_photo);
        } else {
            icon.setImageResource(R.drawable.ic_drawer_folder);
        }
    }

    @Override
    public int getItemCount() {
        // Add 2 for donate and settings
        return mItems.size() + 2;
    }
}
