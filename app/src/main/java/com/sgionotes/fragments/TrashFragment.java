package com.sgionotes.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sgionotes.R;
import com.sgionotes.activities.DetailNoteActivity;
import com.sgionotes.adapters.NoteAdapter;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;

import java.util.List;
import java.util.stream.Collectors;


public class TrashFragment extends Fragment {

    private RecyclerView recyclerTrashNotes;

    private NoteAdapter notaAdapter;
    private List<Note> listaNotasTrash;
    private Button btnVaciarPapelera;

    public TrashFragment() {
        listaNotasTrash = GenerarData.getInstance().getListaNotas()
                .stream()
                .filter(Note::isTrash)
                .collect(Collectors.toList());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_trash, container, false);
        recyclerTrashNotes = vista.findViewById(R.id.recyclerTrashNotes);
        btnVaciarPapelera = vista.findViewById(R.id.btnVaciarPapelera);

        recyclerTrashNotes.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );


        notaAdapter = new NoteAdapter(getContext(), listaNotasTrash);
        recyclerTrashNotes.setAdapter(notaAdapter);

        notaAdapter.setOnItemClickListener(nota -> {
            Intent intent = new Intent(getContext(), DetailNoteActivity.class);
            intent.putExtra("titulo", nota.getTitulo());
            intent.putExtra("contenido", nota.getContenido());
            intent.putExtra("desdePapelera", true);
            startActivity(intent);
        });

//        btnVaciarPapelera.setOnClickListener(btn -> {
//
//        });

        return vista;
    }
}