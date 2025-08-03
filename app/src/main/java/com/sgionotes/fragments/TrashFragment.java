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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sgionotes.R;
import com.sgionotes.activities.DetailNoteActivity;
import com.sgionotes.adapters.NoteAdapter;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;
import com.sgionotes.repository.FirestoreRepository;
import java.util.List;
import java.util.stream.Collectors;

public class TrashFragment extends Fragment implements GenerarData.DataChangeListener {

    private RecyclerView recyclerTrashNotes;
    private NoteAdapter notaAdapter;
    private List<Note> listaNotasTrash;
    private Button btnVaciarPapelera;
    private GenerarData generarData;

    public TrashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_trash, container, false);
        generarData = GenerarData.getInstancia();
        generarData.addDataChangeListener(this);
        recyclerTrashNotes = vista.findViewById(R.id.recyclerTrashNotes);
        btnVaciarPapelera = vista.findViewById(R.id.btnVaciarPapelera);
        recyclerTrashNotes.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        cargarNotasPapelera();
        notaAdapter = new NoteAdapter(getContext(), listaNotasTrash);
        recyclerTrashNotes.setAdapter(notaAdapter);
        notaAdapter.setOnItemClickListener(nota -> {
            mostrarDialogoEliminarNotaIndividual(nota);
        });
        btnVaciarPapelera.setOnClickListener(btn -> {
            mostrarDialogoVaciarPapelera();
        });

        return vista;
    }

    private void cargarNotasPapelera() {
        listaNotasTrash = generarData.getListaNotas()
                .stream()
                .filter(Note::isTrash)
                .collect(Collectors.toList());
    }

    private void mostrarDialogoEliminarNotaIndividual(Note nota) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext())
                .setMessage("La nota se eliminará permanentemente")
                .setPositiveButton("Aceptar", (dialogInterface, which) -> {
                    eliminarNotaIndividual(nota.getId());
                })
                .setNegativeButton("Cancelar", null);

        androidx.appcompat.app.AlertDialog alertDialog = dialog.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            boolean isDarkMode = (getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int buttonColor = isDarkMode ?
                    getResources().getColor(R.color.purple, requireContext().getTheme()) :
                    getResources().getColor(R.color.cian, requireContext().getTheme());
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(buttonColor);
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(buttonColor);
        });
        alertDialog.show();
    }

    private void eliminarNotaIndividual(String noteId) {
        generarData.getFirestoreRepository().deleteNotePermanently(noteId, new FirestoreRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        generarData.refreshDataForCurrentUser();
                        Toast.makeText(getContext(), "Nota eliminada permanentemente", Toast.LENGTH_SHORT).show();
                        actualizarLista();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error al eliminar nota: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void mostrarDialogoVaciarPapelera() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext())
                .setMessage("¿Está seguro de eliminar permanentemente las notas?")
                .setPositiveButton("Aceptar", (dialogInterface, which) -> {
                    vaciarPapelera();
                })
                .setNegativeButton("Cancelar", null);

        androidx.appcompat.app.AlertDialog alertDialog = dialog.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            boolean isDarkMode = (getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int buttonColor = isDarkMode ?
                    getResources().getColor(R.color.purple, requireContext().getTheme()) :
                    getResources().getColor(R.color.cian, requireContext().getTheme());
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(buttonColor);
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(buttonColor);
        });
        alertDialog.show();
    }

    private void vaciarPapelera() {
        List<Note> notasEnPapelera = generarData.getListaNotas()
                .stream()
                .filter(Note::isTrash)
                .collect(Collectors.toList());

        if (notasEnPapelera.isEmpty()) {
            Toast.makeText(getContext(), "La papelera ya está vacía", Toast.LENGTH_SHORT).show();
            return;
        }
        int totalNotas = notasEnPapelera.size();
        int[] notasEliminadas = {0};

        for (Note nota : notasEnPapelera) {
            generarData.getFirestoreRepository().deleteNotePermanently(nota.getId(), new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    notasEliminadas[0]++;
                    if (notasEliminadas[0] == totalNotas) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Papelera vaciada correctamente", Toast.LENGTH_SHORT).show();
                                actualizarLista();
                            });
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error al vaciar papelera: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    private void actualizarLista() {
        cargarNotasPapelera();
        if (notaAdapter != null) {
            notaAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::actualizarLista);
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
    public void onResume() {
        super.onResume();
        actualizarLista();
    }
}