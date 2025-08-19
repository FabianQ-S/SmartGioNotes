package com.sgionotes.activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.sgionotes.R;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;
import com.sgionotes.repository.FirestoreRepository;
import java.util.ArrayList;
import java.util.List;

public class DetailNoteActivity extends AppCompatActivity {

    private ArrayList<String> etiquetasNota;
    private ArrayList<String> etiquetasNotaIds; //ID
    private TextView txtIdNotaDetailNote;
    private EditText etTitulo;
    private EditText etContenido;
    private LinearLayout detailNote;
    private ChipGroup chipGroupSelectedTags;
    private boolean desdePapelera = false;
    private boolean enviandoAPapelera = false;

    private final ActivityResultLauncher<Intent> tagsActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> selectedTags = result.getData().getStringArrayListExtra("selectedTags");
                    if (selectedTags != null) {
                        etiquetasNota = selectedTags;
                        convertTagNamesToIds(selectedTags);
                        updateSelectedTagsDisplay();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        txtIdNotaDetailNote = findViewById(R.id.txtIdNotaDetailNote);
        etTitulo = findViewById(R.id.etTitulo);
        etContenido = findViewById(R.id.etmDetalleNota);
        detailNote = findViewById(R.id.detailNote);
        chipGroupSelectedTags = findViewById(R.id.chipGroupSelectedTags);
        etiquetasNota = new ArrayList<>();
        etiquetasNotaIds = new ArrayList<>();

        boolean esNueva = getIntent().getBooleanExtra("esNueva", false);

        Intent intent = getIntent();
        if (intent != null) {
            String id = intent.getStringExtra("id");
            String titulo = intent.getStringExtra("titulo");
            String contenido = intent.getStringExtra("contenido");
            desdePapelera = intent.getBooleanExtra("desdePapelera", false);
            ArrayList<String> intentTagIds = intent.getStringArrayListExtra("etiquetas");

            txtIdNotaDetailNote.setText(id);
            etTitulo.setText(titulo);
            etContenido.setText(contenido);

            if (intentTagIds != null && !intentTagIds.isEmpty()) {
                etiquetasNotaIds = new ArrayList<>(intentTagIds);
                loadTagNamesFromIds(etiquetasNotaIds);
            } else {
                updateSelectedTagsDisplay();
            }

            if (desdePapelera) {
                etTitulo.setFocusable(false);
                etTitulo.setClickable(true);
                etContenido.setFocusable(false);
                etContenido.setClickable(true);
                View.OnClickListener mostrarMensaje = v -> mostrarDialogoEliminarNotaIndividual(id);
                etTitulo.setOnClickListener(mostrarMensaje);
                etContenido.setOnClickListener(mostrarMensaje);
            }
        } else {
            updateSelectedTagsDisplay();
        }
        configurarVisibilidadBotones(esNueva, desdePapelera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailNote), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.fabEtiquetas).setOnClickListener(v -> {
            if (!desdePapelera) {
                Intent intentTags = new Intent(DetailNoteActivity.this, TagsActivity.class);
                if (etiquetasNota != null) {
                    intentTags.putStringArrayListExtra("tags", etiquetasNota);
                }
                tagsActivityLauncher.launch(intentTags);
            } else {
                Snackbar.make(detailNote, "No es posible editar etiquetas desde la papelera", Snackbar.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.fabEliminar).setOnClickListener(v -> {
            if (desdePapelera) {
                mostrarDialogoEliminarNotaIndividual(txtIdNotaDetailNote.getText().toString());
            } else {
                mostrarDialogoEnviarPapelera();
            }
        });

        findViewById(R.id.fabUbicacion).setOnClickListener(v ->
            Toast.makeText(this, "Función GPS próximamente", Toast.LENGTH_SHORT).show());
    }

    private void updateSelectedTagsDisplay() {
        chipGroupSelectedTags.removeAllViews();

        if (etiquetasNota != null && !etiquetasNota.isEmpty()) {
            chipGroupSelectedTags.setVisibility(View.VISIBLE);

            for (String tagName : etiquetasNota) {
                Chip chip = createFilterChip(tagName);
                chipGroupSelectedTags.addView(chip);
            }
        } else {
            chipGroupSelectedTags.setVisibility(View.GONE);
        }
    }

    private Chip createFilterChip(String tagName) {
        Chip chip = new Chip(this);
        chip.setText(tagName);
        chip.setTextSize(14f);

        // ColoresPorTema
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            chip.setChipBackgroundColorResource(R.color.purple);
            chip.setTextColor(getResources().getColor(R.color.white, getTheme()));
        } else {
            chip.setChipBackgroundColorResource(R.color.chipSelectedBackground);
            chip.setTextColor(getResources().getColor(R.color.chipSelectedText, getTheme()));
        }

        chip.setChipStrokeWidth(0f);
        chip.setCloseIconVisible(true);
        chip.setCloseIconResource(android.R.drawable.ic_menu_close_clear_cancel);
        chip.setClickable(false);
        chip.setCheckable(false);

        chip.setOnCloseIconClickListener(v -> {
            try {
                // Validacion
                if (etiquetasNota == null || etiquetasNotaIds == null) {
                    Toast.makeText(DetailNoteActivity.this, "Error: Listas de etiquetas no inicializadas", Toast.LENGTH_SHORT).show();
                    return;
                }

                int index = etiquetasNota.indexOf(tagName);
                if (index != -1 && index < etiquetasNota.size() && index < etiquetasNotaIds.size()) {
                    String removedTagId = etiquetasNotaIds.get(index);
                    etiquetasNota.remove(index);
                    etiquetasNotaIds.remove(index);
                    updateSelectedTagsDisplay();
                    guardarNotaInmediatamente();
                    // ForzarActualizacion
                    GenerarData.getInstancia().forceSyncData();
                    Toast.makeText(DetailNoteActivity.this, "Etiqueta eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DetailNoteActivity.this, "Recargando etiquetas...", Toast.LENGTH_SHORT).show();
                    recargarEtiquetasDesdeFirebase();
                }
            } catch (Exception e) {
                Toast.makeText(DetailNoteActivity.this, "Error al eliminar etiqueta", Toast.LENGTH_SHORT).show();
                recargarEtiquetasDesdeFirebase();
            }
        });

        return chip;
    }
    private void guardarNotaInmediatamente() {
        GenerarData generarData = GenerarData.getInstancia();
        String id = txtIdNotaDetailNote.getText().toString();
        String titulo = etTitulo.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();

        if (titulo.isEmpty() && contenido.isEmpty()) {
            return;
        }

        Note nota = new Note();
        nota.setId(id.isEmpty() ? null : id);
        nota.setTitulo(titulo);
        nota.setContenido(contenido);
        nota.setTagIds(new ArrayList<>(etiquetasNotaIds));
        nota.setTimestamp(System.currentTimeMillis());

        generarData.getFirestoreRepository().saveNote(nota, new FirestoreRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (nota.getId() != null && !nota.getId().isEmpty()) {
                        txtIdNotaDetailNote.setText(nota.getId());
                    }
                    generarData.updateNoteInLocalList(nota);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DetailNoteActivity.this, "Error guardando nota", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void recargarEtiquetasDesdeFirebase() {
        String noteId = txtIdNotaDetailNote.getText().toString();
        if (noteId == null || noteId.isEmpty()) {
            return;
        }
        GenerarData generarData = GenerarData.getInstancia();
        generarData.getFirestoreRepository().getNoteById(noteId, new FirestoreRepository.DataCallback<Note>() {
            @Override
            public void onSuccess(Note note) {
                runOnUiThread(() -> {
                    if (note.getTagIds() != null) {
                        etiquetasNotaIds = new ArrayList<>(note.getTagIds());
                        loadTagNamesFromIds(etiquetasNotaIds);
                    } else {
                        etiquetasNotaIds.clear();
                        etiquetasNota.clear();
                        updateSelectedTagsDisplay();
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DetailNoteActivity.this, "Error recargando etiquetas", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void configurarVisibilidadBotones(boolean esNueva, boolean desdePapelera) {
        View fabEtiquetas = findViewById(R.id.fabEtiquetas);
        View fabEliminar = findViewById(R.id.fabEliminar);
        View fabUbicacion = findViewById(R.id.fabUbicacion);
        View containerEliminar = (View) fabEliminar.getParent();
        View containerUbicacion = (View) fabUbicacion.getParent();
        View containerEtiquetas = (View) fabEtiquetas.getParent();
        if (esNueva) {
            containerEtiquetas.setVisibility(View.VISIBLE);
            containerEliminar.setVisibility(View.GONE);
            containerUbicacion.setVisibility(View.VISIBLE);
        } else if (desdePapelera) {
            containerEtiquetas.setVisibility(View.GONE);
            containerEliminar.setVisibility(View.VISIBLE);
            containerUbicacion.setVisibility(View.GONE);
        } else {
            containerEtiquetas.setVisibility(View.VISIBLE);
            containerEliminar.setVisibility(View.VISIBLE);
            containerUbicacion.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarDialogoEnviarPapelera() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this)
                .setMessage("La nota se enviará a papelera")
                .setPositiveButton("Aceptar", (dialogInterface, which) -> {
                    enviarNotaAPapelera();
                })
                .setNegativeButton("Cancelar", null);
        androidx.appcompat.app.AlertDialog alertDialog = dialog.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            boolean isDarkMode = (getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int buttonColor = isDarkMode ?
                    getResources().getColor(R.color.purple, getTheme()) :
                    getResources().getColor(R.color.cian, getTheme());
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(buttonColor);
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(buttonColor);
        });
        alertDialog.show();
    }
    private void mostrarDialogoEliminarNotaIndividual(String noteId) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this)
                .setMessage("La nota se eliminará permanentemente")
                .setPositiveButton("Aceptar", (dialogInterface, which) -> {
                    eliminarNotaPermanentemente(noteId);
                })
                .setNegativeButton("Cancelar", null);
        androidx.appcompat.app.AlertDialog alertDialog = dialog.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            boolean isDarkMode = (getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int buttonColor = isDarkMode ?
                    getResources().getColor(R.color.purple, getTheme()) :
                    getResources().getColor(R.color.cian, getTheme());
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(buttonColor);
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(buttonColor);
        });
        alertDialog.show();
    }
    private void enviarNotaAPapelera() {
        String noteId = txtIdNotaDetailNote.getText().toString();
        GenerarData generarData = GenerarData.getInstancia();

        if (noteId != null && !noteId.isEmpty()) {
            enviandoAPapelera = true;
            generarData.getFirestoreRepository().moveNoteToTrash(noteId, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailNoteActivity.this, "Nota enviada a papelera", Toast.LENGTH_SHORT).show();
                        new android.os.Handler().postDelayed(() -> {
                            generarData.refreshDataForCurrentUser();
                            Intent intent = new Intent(DetailNoteActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }, 500);
                    });
                }
                @Override
                public void onError(String error) {
                    enviandoAPapelera = false;
                    runOnUiThread(() -> {
                        Toast.makeText(DetailNoteActivity.this, "Error al enviar a papelera: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Toast.makeText(this, "Error: ID de nota no válido", Toast.LENGTH_SHORT).show();
        }
    }
    private void guardarNotaAntesDePapelera() {
        String id = txtIdNotaDetailNote.getText().toString();
        String titulo = etTitulo.getText().toString();
        String contenido = etContenido.getText().toString();

        if ((titulo.trim().isEmpty() && contenido.trim().isEmpty()) || id == null || id.isEmpty()) {
            return;
        }
        Note notaActualizada = new Note(titulo, contenido);
        notaActualizada.setId(id);
        if (etiquetasNotaIds != null) {
            notaActualizada.setTagIds(etiquetasNotaIds);
        }
        GenerarData generarData = GenerarData.getInstancia();
        generarData.getFirestoreRepository().saveNote(notaActualizada, new FirestoreRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onError(String error) {
            }
        });
    }

    private void eliminarNotaPermanentemente(String noteId) {
        GenerarData generarData = GenerarData.getInstancia();
        if (noteId != null && !noteId.isEmpty()) {
            generarData.getFirestoreRepository().deleteNotePermanently(noteId, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailNoteActivity.this, "Nota eliminada permanentemente", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailNoteActivity.this, "Error al eliminar nota: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!desdePapelera && !enviandoAPapelera) {
            guardarNota();
        }
    }

    private void guardarNota() {
        String id = txtIdNotaDetailNote.getText().toString();
        String titulo = etTitulo.getText().toString();
        String contenido = etContenido.getText().toString();

        if (titulo.trim().isEmpty() && contenido.trim().isEmpty()) {
            return;
        }

        GenerarData generarData = GenerarData.getInstancia();

        if (id == null || id.isEmpty()) {
            Note nuevaNota = new Note(titulo, contenido);
            if (etiquetasNotaIds != null) {
                nuevaNota.setTagIds(etiquetasNotaIds);
            }
            generarData.getFirestoreRepository().saveNote(nuevaNota, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        if (nuevaNota.getId() != null) {
                            txtIdNotaDetailNote.setText(nuevaNota.getId());
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(DetailNoteActivity.this, "Error al guardar nota: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            generarData.getFirestoreRepository().getNoteById(id, new FirestoreRepository.DataCallback<Note>() {
                @Override
                public void onSuccess(Note notaExistente) {
                    if (notaExistente != null) {
                        notaExistente.setTitulo(titulo);
                        notaExistente.setContenido(contenido);
                        if (etiquetasNotaIds != null) {
                            notaExistente.setTagIds(etiquetasNotaIds);
                        }

                        generarData.getFirestoreRepository().saveNote(notaExistente, new FirestoreRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(DetailNoteActivity.this, "Error al actualizar nota: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Note notaActualizada = new Note(titulo, contenido);
                    notaActualizada.setId(id);
                    if (etiquetasNotaIds != null) {
                        notaActualizada.setTagIds(etiquetasNotaIds);
                    }
                    generarData.getFirestoreRepository().saveNote(notaActualizada, new FirestoreRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError(String error) {
                            Toast.makeText(DetailNoteActivity.this, "Error al actualizar nota: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void loadTagNamesFromIds(ArrayList<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            etiquetasNota = new ArrayList<>();
            etiquetasNotaIds = new ArrayList<>();
            updateSelectedTagsDisplay();
            return;
        }
        GenerarData generarData = GenerarData.getInstancia();
        generarData.getFirestoreRepository().getTagsByIds(tagIds, new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> tags) {
                runOnUiThread(() -> {
                    etiquetasNota = new ArrayList<>();
                    etiquetasNotaIds = new ArrayList<>(tagIds); //MantenerID
                    for (Tag tag : tags) {
                        etiquetasNota.add(tag.getEtiquetaDescripcion());
                    }
                    updateSelectedTagsDisplay();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    etiquetasNota = new ArrayList<>();
                    etiquetasNotaIds = new ArrayList<>();
                    updateSelectedTagsDisplay();
                });
            }
        });
    }

    private void convertTagNamesToIds(ArrayList<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            etiquetasNotaIds = new ArrayList<>();
            return;
        }

        GenerarData generarData = GenerarData.getInstancia();
        generarData.getFirestoreRepository().getAllTags(new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> allTags) {
                runOnUiThread(() -> {
                    etiquetasNotaIds = new ArrayList<>();
                    for (String tagName : tagNames) {
                        for (Tag tag : allTags) {
                            if (tag.getEtiquetaDescripcion().equals(tagName)) {
                                etiquetasNotaIds.add(tag.getId());
                                break;
                            }
                        }
                    }
                    updateSelectedTagsDisplay();
                    String noteId = txtIdNotaDetailNote.getText().toString();
                    if (noteId == null || noteId.isEmpty()) {
                        guardarNotaConEtiquetasInmediatamente();
                    } else {
                        guardarNotaInmediatamente();
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DetailNoteActivity.this, "Error cargando etiquetas", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void guardarNotaConEtiquetasInmediatamente() {
        String titulo = etTitulo.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();
        if (titulo.isEmpty() && contenido.isEmpty() && (etiquetasNotaIds == null || etiquetasNotaIds.isEmpty())) {
            return; // NoGuardarVacia
        }
        GenerarData generarData = GenerarData.getInstancia();
        Note nuevaNota = new Note(titulo, contenido);
        if (etiquetasNotaIds != null && !etiquetasNotaIds.isEmpty()) {
            nuevaNota.setTagIds(new ArrayList<>(etiquetasNotaIds));
        }
        nuevaNota.setTimestamp(System.currentTimeMillis());
        generarData.addNotaWithImmediateSync(nuevaNota, new FirestoreRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (nuevaNota.getId() != null && !nuevaNota.getId().isEmpty()) {
                        txtIdNotaDetailNote.setText(nuevaNota.getId());
                    }
                    generarData.forceNotifyDataChanged();
                    Toast.makeText(DetailNoteActivity.this, "Nota guardada con etiquetas", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DetailNoteActivity.this, "Error guardando nota: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
