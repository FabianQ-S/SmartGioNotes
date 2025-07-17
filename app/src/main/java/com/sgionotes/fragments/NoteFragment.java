package com.sgionotes.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.ColorStateList;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sgionotes.R;
import com.sgionotes.activities.DetailNoteActivity;
import com.sgionotes.adapters.NoteAdapter;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;

import java.util.ArrayList;
import java.util.List;

public class NoteFragment extends Fragment {

    private RecyclerView recyclerNotas;
    private NoteAdapter notaAdapter;
    private List<Note> listaNotas;
    private String titulo;
    private String contenido;
    FloatingActionButton floatingActionButton;

    public NoteFragment() {
        listaNotas = GenerarData.getInstance().getListaNotas();
    }

    private ActivityResultLauncher<Intent> launchNewNoteActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchNewNoteActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        titulo = result.getData().getStringExtra("titulo");
                        contenido = result.getData().getStringExtra("contenido");

                        if (!titulo.isEmpty() || !contenido.isEmpty()) {
                            Note nuevaNota = new Note(
                                    listaNotas.get(listaNotas.size()-1).getId(),
                                    titulo, contenido, new ArrayList<>(), true, false
                            );
                            listaNotas.add(0, nuevaNota);
                            notaAdapter.notifyItemInserted(0);
                            recyclerNotas.scrollToPosition(0);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_note, container, false);
        recyclerNotas = vista.findViewById(R.id.recyclerNotas);
        floatingActionButton = vista.findViewById(R.id.addNota);

        recyclerNotas.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        notaAdapter = new NoteAdapter(getContext(), listaNotas);
        recyclerNotas.setAdapter(notaAdapter);

        notaAdapter.setOnItemClickListener(nota -> {
            Intent intent = new Intent(getContext(), DetailNoteActivity.class);
            intent.putExtra("titulo", nota.getTitulo());
            intent.putExtra("contenido", nota.getContenido());

            ArrayList<String> etiquetas = new ArrayList<>();
            for (Tag tag : nota.getEtiquetas()) {
                etiquetas.add(tag.getEtiquetaDescripcion());
            }
            intent.putStringArrayListExtra("etiquetas", etiquetas);

            startActivity(intent);
        });

        floatingActionButton.setOnClickListener(btn -> {
            Intent intent = new Intent(getContext(), DetailNoteActivity.class);
            intent.putExtra("esNueva", true);
            launchNewNoteActivity.launch(intent);
        });

        return vista;
    }

}