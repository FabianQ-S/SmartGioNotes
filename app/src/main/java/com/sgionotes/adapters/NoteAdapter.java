package com.sgionotes.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sgionotes.R;
import com.sgionotes.models.Tag;
import com.sgionotes.models.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotaViewHolder> {

    private Context context;
    private List<Note> listaNotas;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Note nota);
    }

    public NoteAdapter(Context context, List<Note> listaNotas) {
        this.context = context;
        this.listaNotas = listaNotas;
    }

    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_tarjeta, parent, false);
        return new NotaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {

        Note nota = listaNotas.get(position);
        holder.txtIdNota.setText(String.valueOf(nota.getId()));
        holder.txtTitulo.setText(nota.getTitulo());
        holder.txtContenido.setText(nota.getContenido());

        // Limpiar chips anteriores
        holder.chipGroup.removeAllViews();
        holder.chipGroup.setVisibility(View.GONE);

        // Mostrar etiquetas si existen tagIds válidos
        if (nota.getTagIds() != null && !nota.getTagIds().isEmpty()) {
            // Filtrar etiquetas válidas (que realmente existen)
            List<String> validTagIds = getValidTagIds(nota.getTagIds());

            if (!validTagIds.isEmpty()) {
                holder.chipGroup.setVisibility(View.VISIBLE);
                LayoutInflater inflater = LayoutInflater.from(context);
                Chip chip = (Chip) inflater.inflate(R.layout.layout_item_chip, holder.chipGroup, false);
                chip.setClickable(false);
                chip.setCheckable(false);

                // Actualizar el contador con etiquetas válidas únicamente
                int validCount = validTagIds.size();
                chip.setText(validCount + " etiqueta" + (validCount > 1 ? "s" : ""));
                holder.chipGroup.addView(chip);

                // Actualizar la nota con las etiquetas válidas para mantener consistencia
                if (validTagIds.size() != nota.getTagIds().size()) {
                    nota.setTagIds(validTagIds);
                    // Notificar a GenerarData que hubo cambios en las etiquetas
                    com.sgionotes.models.GenerarData.getInstancia().forceNotifyDataChanged();
                }
            } else {
                // Si no hay etiquetas válidas, limpiar la lista de tagIds de la nota
                nota.setTagIds(new ArrayList<>());
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(nota);
            }
        });
    }

    // Método para validar que las etiquetas realmente existen
    private List<String> getValidTagIds(List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Obtener lista actual de etiquetas disponibles desde GenerarData
        List<Tag> availableTags = com.sgionotes.models.GenerarData.getInstancia().getListaEtiquetas();
        List<String> availableTagIds = new ArrayList<>();

        if (availableTags != null) {
            for (Tag tag : availableTags) {
                if (tag.getId() != null && !tag.getId().isEmpty()) {
                    availableTagIds.add(tag.getId());
                }
            }
        }

        // Filtrar solo los IDs que realmente existen
        List<String> validIds = new ArrayList<>();
        for (String tagId : tagIds) {
            if (tagId != null && !tagId.isEmpty() && availableTagIds.contains(tagId)) {
                validIds.add(tagId);
            }
        }

        return validIds;
    }

    @Override
    public int getItemCount() {
        return listaNotas.size();
    }

    public void updateNotes(List<Note> newNotes) {
        this.listaNotas = newNotes;
        notifyDataSetChanged(); // Forzar actualización completa del adaptador
    }

    public static class NotaViewHolder extends RecyclerView.ViewHolder {
        TextView txtIdNota;
        TextView txtTitulo;
        TextView txtContenido;
        ChipGroup chipGroup;

        public NotaViewHolder(View itemView) {
            super(itemView);
            txtIdNota = itemView.findViewById(R.id.txtIdNota);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtContenido = itemView.findViewById(R.id.txtContenido);
            chipGroup = itemView.findViewById(R.id.chipGroupEtiquetas);
        }
    }
}
