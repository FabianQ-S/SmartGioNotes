package com.sgionotes.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.sgionotes.R;
import com.sgionotes.adapters.NoteAdapter;
import com.sgionotes.adapters.TagAdapter;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Tag;

import java.util.List;

public class TagFragment extends Fragment {

    private RecyclerView recyclerTags;
    private TagAdapter tagAdapter;
    private List<Tag> listaEtiquetas;
    private EditText txtTagNew;
    private ImageView btnAddTag;

    public TagFragment() {
        listaEtiquetas = GenerarData.getInstance().getListaEtiquetas();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_tag, container, false);

        recyclerTags = vista.findViewById(R.id.recyclerTags);
        txtTagNew = vista.findViewById(R.id.txtTagNew);
        btnAddTag = vista.findViewById(R.id.btnAddTag);

        recyclerTags.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        btnAddTag.setOnClickListener(v -> agregarEtiqueta());

        txtTagNew.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                agregarEtiqueta();
                return true;
            }
            return false;
        });

        tagAdapter = new TagAdapter(listaEtiquetas);
        recyclerTags.setAdapter(tagAdapter);

        return vista;

    }

    private void agregarEtiqueta() {
        String texto = txtTagNew.getText().toString().trim();
        if (!texto.isEmpty()) {
            Tag nueva = new Tag(texto);
            listaEtiquetas.add(0, nueva);
            tagAdapter.notifyItemInserted(0);
            recyclerTags.scrollToPosition(0);
            txtTagNew.setText("");

            txtTagNew.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtTagNew.getWindowToken(), 0);
        } else {
            Snackbar.make(recyclerTags, "La etiqueta no puede estar vacía", Snackbar.LENGTH_SHORT).show();
        }
    }

}