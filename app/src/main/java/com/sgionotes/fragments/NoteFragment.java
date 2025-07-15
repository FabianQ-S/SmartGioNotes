package com.sgionotes.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sgionotes.R;
import com.sgionotes.adapters.NoteAdapter;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;
import java.util.List;

public class NoteFragment extends Fragment {

    private GenerarData generarData;
    private RecyclerView recyclerNotas;
    private NoteAdapter notaAdapter;
    private List<Note> listaNotas;

    public NoteFragment() {
        generarData = new GenerarData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_note, container, false);

        recyclerNotas = vista.findViewById(R.id.recyclerNotas);

        // 2 columnas
        recyclerNotas.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        // Data estatica
        listaNotas = generarData.getListaNotas();

        notaAdapter = new NoteAdapter(getContext(), listaNotas);
        recyclerNotas.setAdapter(notaAdapter);

//        recyclerNotas.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.navMenu);
//
//                if (dy > 10) {
//                    bottomNav.animate().translationY(bottomNav.getHeight()).setDuration(200);
//                } else if (dy < -10) {
//                    bottomNav.animate().translationY(0).setDuration(200);
//                }
//            }
//        });

        return vista;
    }

}