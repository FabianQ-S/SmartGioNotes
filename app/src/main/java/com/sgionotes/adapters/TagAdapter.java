package com.sgionotes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sgionotes.R;
import com.sgionotes.models.Tag;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<Tag> listaEtiquetas;

    public TagAdapter(List<Tag> listaEtiquetas) {
        this.listaEtiquetas = listaEtiquetas;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_tag_edit, parent, false);
        return new TagAdapter.TagViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {

        Tag etiqueta = listaEtiquetas.get(position);
        holder.txtEtiqueta.setText(etiqueta.getEtiquetaDescripcion());

    }

    @Override
    public int getItemCount() {
        return listaEtiquetas.size();
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        EditText txtEtiqueta;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEtiqueta = itemView.findViewById(R.id.txtTagEditable);
        }
    }

}
