package com.sgionotes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sgionotes.R;
import com.sgionotes.models.Tag;
import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagViewHolder> {

    private List<Tag> listaTags;
    private OnTagDeleteListener deleteListener;

    public interface OnTagDeleteListener {
        void onTagDelete(Tag tag, int position);
    }

    public TagsAdapter(List<Tag> listaTags) {
        this.listaTags = listaTags;
    }

    public void setOnTagDeleteListener(OnTagDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        if (position >= 0 && position < listaTags.size()) {
            Tag tag = listaTags.get(position);
            if (tag != null) {
                holder.tvTagName.setText(tag.getEtiquetaDescripcion());
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaTags != null ? listaTags.size() : 0;
    }

    // Método seguro para eliminar etiqueta
    public void removeTag(int position) {
        if (position >= 0 && position < listaTags.size()) {
            Tag removedTag = listaTags.get(position);
            listaTags.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listaTags.size());

            // Notificar al listener
            if (deleteListener != null) {
                deleteListener.onTagDelete(removedTag, position);
            }
        }
    }

    // Método para actualizar la lista de etiquetas
    public void updateTags(List<Tag> newTags) {
        if (newTags != null) {
            this.listaTags = newTags;
            notifyDataSetChanged();
        }
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagName;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagName = itemView.findViewById(R.id.tvTagName);
        }
    }
}
