package com.sgionotes.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sgionotes.R;
import com.sgionotes.activities.DetailNoteActivity;
import com.sgionotes.adapters.NoteAdapter;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;
import java.util.List;

public class NoteFragment extends Fragment {

    private GenerarData generarData;
    private RecyclerView recyclerNotas;
    private NoteAdapter notaAdapter;
    private List<Note> listaNotas;
    FloatingActionButton floatingActionButton;

    public NoteFragment() {
        generarData = new GenerarData(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_note, container, false);
        recyclerNotas = vista.findViewById(R.id.recyclerNotas);
        floatingActionButton = vista.findViewById(R.id.addNota);

        recyclerNotas.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        // Data estatica
        listaNotas = generarData.getListaNotas();

        notaAdapter = new NoteAdapter(getContext(), listaNotas);
        recyclerNotas.setAdapter(notaAdapter);

        notaAdapter.setOnItemClickListener(nota -> {
            Intent intent = new Intent(getContext(), DetailNoteActivity.class);
            intent.putExtra("titulo", nota.getTitulo());
            intent.putExtra("contenido", nota.getContenido());
            startActivity(intent);
        });

        floatingActionButton.setOnClickListener(btn -> {
            Intent intent = new Intent(getContext(), DetailNoteActivity.class);
            startActivity(intent);
        });

        return vista;
    }

}