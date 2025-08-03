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

public class NoteFragment extends Fragment implements GenerarData.DataChangeListener {
    private RecyclerView recyclerNotas;
    private NoteAdapter notaAdapter;
    private List<Note> listaNotas;
    private String titulo;
    private String contenido;
    private int lastId;
    private FloatingActionButton floatingActionButton;
    private GenerarData generarData;
    public NoteFragment() {
    }
    private ActivityResultLauncher<Intent> launchNewNoteActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchNewNoteActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        boolean esNueva = data.getBooleanExtra("esNueva", false);
                        if (esNueva) {
                            titulo = data.getStringExtra("titulo");
                            contenido = data.getStringExtra("contenido");
                            if (!titulo.isEmpty() || !contenido.isEmpty()) {
                                Note nuevaNota = new Note(titulo, contenido);
                                GenerarData.getInstance().getFirestoreRepository()
                                        .saveNote(nuevaNota, new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                                            @Override
                                            public void onSuccess() {
                                                listaNotas.add(0, nuevaNota);
                                                notaAdapter.notifyItemInserted(0);
                                                recyclerNotas.scrollToPosition(0);
                                            }
                                            @Override
                                            public void onError(String error) {
                                                //Error
                                                if (getContext() != null) {
                                                    android.widget.Toast.makeText(getContext(),
                                                            "Error al guardar la nota: " + error,
                                                            android.widget.Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            int position = data.getIntExtra("position", -1);
                            if (position != -1) {
                                notaAdapter.notifyItemChanged(position);
                            }
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

        generarData = GenerarData.getInstance();
        generarData.addDataChangeListener(this);

        //NotasActivas
        listaNotas = getActiveNotes();
        notaAdapter = new NoteAdapter(getContext(), listaNotas);
        recyclerNotas.setAdapter(notaAdapter);

        notaAdapter.setOnItemClickListener(nota -> {
            int position = listaNotas.indexOf(nota);
            Intent intent = new Intent(getContext(), DetailNoteActivity.class);
            intent.putExtra("id", String.valueOf(nota.getId()));
            intent.putExtra("titulo", nota.getTitulo());
            intent.putExtra("contenido", nota.getContenido());
            intent.putExtra("estaCreado", true);
            intent.putExtra("position", position);
            ArrayList<String> etiquetas = new ArrayList<>();
            for (Tag tag : nota.getEtiquetas()) {
                etiquetas.add(tag.getEtiquetaDescripcion());
            }
            intent.putStringArrayListExtra("etiquetas", etiquetas);
            launchNewNoteActivity.launch(intent);
        });
        floatingActionButton.setOnClickListener(btn -> {
            Intent intent = new Intent(getContext(), DetailNoteActivity.class);
            intent.putExtra("esNueva", true);
            launchNewNoteActivity.launch(intent);
        });
        return vista;
    }
    private List<Note> getActiveNotes() {
        List<Note> activeNotes = new ArrayList<>();
        if (generarData != null) {
            for (Note note : generarData.getListaNotas()) {
                if (!note.isTrash()) {
                    activeNotes.add(note);
                }
            }
        }
        return activeNotes;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (generarData != null) {
            listaNotas = getActiveNotes();
            if (notaAdapter != null) {
                notaAdapter.updateNotes(listaNotas);
                notaAdapter.notifyDataSetChanged();
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (generarData != null) {
            generarData.removeDataChangeListener(this);
        }
    }
    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (generarData != null) {
                    List<Note> updatedActiveNotes = getActiveNotes();
                    listaNotas = updatedActiveNotes;
                    if (notaAdapter != null) {
                        notaAdapter.updateNotes(listaNotas);
                        notaAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
