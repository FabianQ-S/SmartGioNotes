package com.sgionotes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sgionotes.R;
import com.sgionotes.models.Tag;
import java.util.List;

public class EditableTagAdapter extends RecyclerView.Adapter<EditableTagAdapter.TagViewHolder> {

    private final List<Tag> tags;
    private final Context context;
    private final OnTagActionListener listener;

    public interface OnTagActionListener {
        void onTagEdited(int position, String newText);
        void onTagDeleted(int position);
        void onTagAddedToFavorites(int position);
    }

    public EditableTagAdapter(Context context, List<Tag> tags, OnTagActionListener listener) {
        this.context = context;
        this.tags = tags;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tag_editable, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tags.get(position);
        holder.etTagText.setText(tag.getDisplayText());

        // VerificacionDeFavorito
        if (tag.isFavorite()) {
            holder.ivTagIcon.setVisibility(View.GONE);
            holder.ivFavoriteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivTagIcon.setVisibility(View.VISIBLE);
            holder.ivFavoriteIcon.setVisibility(View.GONE);
        }

        holder.btnMore.setOnClickListener(v -> showPopupMenu(v, position));

        holder.etTagText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                finishEditing(holder, position);
                return true;
            }
            return false;
        });

        holder.etTagText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                finishEditing(holder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    private void showPopupMenu(View anchor, int position) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_tag, null, false);

        // PopupMedir
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();

        PopupWindow popupWindow = new PopupWindow(popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT, true);

        LinearLayout menuAddFavorite = popupView.findViewById(R.id.menuAddFavorite);
        LinearLayout menuEdit = popupView.findViewById(R.id.menuEdit);
        LinearLayout menuDelete = popupView.findViewById(R.id.menuDelete);

        Tag currentTag = tags.get(position);
        TextView favoriteText = (TextView) ((LinearLayout) menuAddFavorite).getChildAt(1);
        if (currentTag.isFavorite()) {
            favoriteText.setText("Quitar de favoritas");
        } else {
            favoriteText.setText("Añadir a favoritas");
        }

        menuAddFavorite.setOnClickListener(v -> {
            if (currentTag.isFavorite()) {
                currentTag.setFavorite(false);
            } else {
                currentTag.setFavorite(true);
            }
            if (listener != null) {
                listener.onTagAddedToFavorites(position);
            }
            sortTags();
            notifyDataSetChanged();
            popupWindow.dismiss();
        });

        menuEdit.setOnClickListener(v -> {
            enableEditing(position);
            popupWindow.dismiss();
        });

        menuDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTagDeleted(position);
            }
            popupWindow.dismiss();
        });

        // CalcularPosicion
        int xOffset = anchor.getWidth() - popupWidth;
        popupWindow.showAsDropDown(anchor, xOffset, 0);
    }

    private void enableEditing(int position) {
        notifyItemChanged(position);
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            RecyclerView.ViewHolder viewHolder = ((RecyclerView) ((android.app.Activity) context).findViewById(R.id.recyclerViewTags))
                    .findViewHolderForAdapterPosition(position);
            if (viewHolder instanceof TagViewHolder) {
                TagViewHolder holder = (TagViewHolder) viewHolder;
                holder.etTagText.setFocusable(true);
                holder.etTagText.setFocusableInTouchMode(true);
                holder.etTagText.requestFocus();
                holder.etTagText.selectAll();

                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(holder.etTagText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void finishEditing(TagViewHolder holder, int position) {
        String newText = holder.etTagText.getText().toString().trim();
        if (!newText.isEmpty() && listener != null) {
            listener.onTagEdited(position, newText);
        }

        holder.etTagText.setFocusable(false);
        holder.etTagText.setFocusableInTouchMode(false);
        holder.etTagText.clearFocus();

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(holder.etTagText.getWindowToken(), 0);
    }

    private void sortTags() {
        tags.sort((tag1, tag2) -> {
            if (tag1.isFavorite() && !tag2.isFavorite()) {
                return -1;
            } else if (!tag1.isFavorite() && tag2.isFavorite()) {
                return 1;
            } else if (tag1.isFavorite() && tag2.isFavorite()) {
                return Long.compare(tag1.getFavoriteTimestamp(), tag2.getFavoriteTimestamp());
            } else {
                return 0;
            }
        });
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        EditText etTagText;
        ImageButton btnMore;
        ImageView ivFavoriteIcon;
        ImageView ivTagIcon;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            etTagText = itemView.findViewById(R.id.etTagText);
            btnMore = itemView.findViewById(R.id.btnMore);
            ivFavoriteIcon = itemView.findViewById(R.id.ivFavoriteIcon);
            ivTagIcon = itemView.findViewById(R.id.ivTagIcon);
        }
    }
}
