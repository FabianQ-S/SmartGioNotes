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

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotaViewHolder> {

    private Context context;
    private List<Note> listaNotas;

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
        holder.txtTitulo.setText(nota.getTitulo());
        holder.txtContenido.setText(nota.getContenido());

        // Limpia los chips anteriores al reciclar vista
        holder.chipGroup.removeAllViews();

        for (Tag tag : nota.getEtiquetas()) {
            Chip chip = new Chip(context);

            chip.setText(tag.getEtiquetaDescripcion());
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

            chip.setChipBackgroundColorResource(R.color.primaryDark);
            chip.setTextColor(ContextCompat.getColor(context, R.color.white));

            chip.setClickable(false);
            chip.setCheckable(false);

            holder.chipGroup.addView(chip);
        }

    }

    @Override
    public int getItemCount() {
        return listaNotas.size();
    }

    public static class NotaViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        TextView txtContenido;
        ChipGroup chipGroup;

        public NotaViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtContenido = itemView.findViewById(R.id.txtContenido);
            chipGroup = itemView.findViewById(R.id.chipGroupEtiquetas);
        }
    }
}
