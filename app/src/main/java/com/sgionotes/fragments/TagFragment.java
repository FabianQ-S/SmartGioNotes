package com.sgionotes.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sgionotes.R;
import com.sgionotes.adapters.NoteAdapter;
import com.sgionotes.adapters.TagAdapter;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Tag;

import java.util.List;

public class TagFragment extends Fragment {

    private GenerarData generarData;
    private RecyclerView recyclerTags;
    private TagAdapter tagAdapter;
    private List<Tag> listaEtiquetas;

    public TagFragment() {
        generarData = new GenerarData(2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_tag, container, false);
        recyclerTags = vista.findViewById(R.id.recyclerTags);

        recyclerTags.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        // Data estatica
        listaEtiquetas = generarData.getListaEtiquetas();

        tagAdapter = new TagAdapter(listaEtiquetas);
        recyclerTags.setAdapter(tagAdapter);

        return vista;

    }
}