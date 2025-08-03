package com.sgionotes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sgionotes.R;
import com.sgionotes.utils.UserProfileManager;
public class ProfileIconDialog {
    public interface OnIconSelectedListener {
        void onIconSelected(int iconResId);
    }
    public static void showIconSelectionDialog(Context context, OnIconSelectedListener listener) {
        try {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_profile_icons, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerProfileIcons);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setHasFixedSize(true);
            Dialog dialog = new MaterialAlertDialogBuilder(context)
                    .setView(dialogView)
                    .setNegativeButton("Cancelar", null)
                    .create();
            IconAdapter adapter = new IconAdapter(UserProfileManager.PROFILE_ICONS, (iconResId) -> {
                if (listener != null) {
                    listener.onIconSelected(iconResId);
                }
                dialog.dismiss();
            });
            recyclerView.setAdapter(adapter);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {
        private final int[] icons;
        private final OnIconSelectedListener listener;
        public IconAdapter(int[] icons, OnIconSelectedListener listener) {
            this.icons = icons;
            this.listener = listener;
        }
        @NonNull
        @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_profile_icon, parent, false);
            return new IconViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            int iconResId = icons[position];
            holder.imageView.setImageResource(iconResId);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIconSelected(iconResId);
                }
            });
        }
        @Override
        public int getItemCount() {
            return icons.length;
        }
        static class IconViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            IconViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imgProfileIcon);
            }
        }
    }
}
