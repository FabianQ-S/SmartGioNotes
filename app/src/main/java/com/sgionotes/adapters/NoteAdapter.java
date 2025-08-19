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
        holder.chipGroup.removeAllViews();
        holder.chipGroup.setVisibility(View.GONE);

        if (nota.getTagIds() != null && !nota.getTagIds().isEmpty()) {
            int validTagCount = com.sgionotes.models.GenerarData.getInstancia().getValidTagCountForNote(nota);

            if (validTagCount > 0) {
                holder.chipGroup.setVisibility(View.VISIBLE);
                LayoutInflater inflater = LayoutInflater.from(context);
                Chip chip = (Chip) inflater.inflate(R.layout.layout_item_chip, holder.chipGroup, false);
                chip.setClickable(false);
                chip.setCheckable(false);

                // MostrarContadorActualizado
                chip.setText(validTagCount + " etiqueta" + (validTagCount > 1 ? "s" : ""));
                holder.chipGroup.addView(chip);
                List<String> validTagIds = getValidTagIds(nota.getTagIds());
                if (validTagIds.size() != nota.getTagIds().size()) {
                    nota.setTagIds(validTagIds);
                    updateNoteInFirebase(nota);
                }
            } else {
                if (!nota.getTagIds().isEmpty()) {
                    nota.setTagIds(new ArrayList<>());
                    updateNoteInFirebase(nota);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(nota);
            }
        });
    }
    private List<String> getValidTagIds(List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> validTagIds = new ArrayList<>();
        List<Tag> allTags = com.sgionotes.models.GenerarData.getInstancia().getListaEtiquetas();
        if (allTags != null) {
            for (String tagId : tagIds) {
                boolean tagExists = false;
                for (Tag tag : allTags) {
                    if (tag.getId() != null && tag.getId().equals(tagId)) {
                        tagExists = true;
                        break;
                    }
                }
                if (tagExists) {
                    validTagIds.add(tagId);
                }
            }
        }

        return validTagIds;
    }

    // MetodoParche
    private void updateNoteInFirebase(Note note) {
        com.sgionotes.models.GenerarData generarData = com.sgionotes.models.GenerarData.getInstancia();
        if (generarData.getFirestoreRepository() != null) {
            generarData.getFirestoreRepository().saveNote(note, new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError(String error) {
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaNotas != null ? listaNotas.size() : 0;
    }
    public void updateNotes(List<Note> newNotes) {
        if (newNotes != null) {
            this.listaNotas = newNotes;
            notifyDataSetChanged();
        }
    }
    public static class NotaViewHolder extends RecyclerView.ViewHolder {
        TextView txtIdNota, txtTitulo, txtContenido;
        ChipGroup chipGroup;
        public NotaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIdNota = itemView.findViewById(R.id.txtIdNota);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtContenido = itemView.findViewById(R.id.txtContenido);
            chipGroup = itemView.findViewById(R.id.chipGroupEtiquetas); // Corregido el ID
        }
    }
}
