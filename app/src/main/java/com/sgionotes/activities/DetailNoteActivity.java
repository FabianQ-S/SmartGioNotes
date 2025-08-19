package com.sgionotes.activities;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.sgionotes.R;
import com.sgionotes.dialogs.LocationConfirmationDialog;
import com.sgionotes.dialogs.LocationDetailsDialog;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;
import com.sgionotes.repository.FirestoreRepository;
import com.sgionotes.utils.LocationHelper;
import com.sgionotes.utils.TagDebugLogger;
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

    // VariablesGPS
    private CardView cardLocationContainer;
    private TextView txtLocationInfo;
    private TextView txtLocationLabel;
    private ImageButton btnRemoveLocation;
    private LinearLayout layoutLocationInfo;
    private String currentGpsLocation = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final ActivityResultLauncher<Intent> tagsActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                String noteId = txtIdNotaDetailNote.getText().toString();
                TagDebugLogger.logMethodCall("TagsActivityResult", noteId, "ResultCode: " + result.getResultCode());

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> selectedTags = result.getData().getStringArrayListExtra("selectedTags");
                    TagDebugLogger.logTagOperation("TAGS_RESULT_RECEIVED", noteId, "Selected tags: " + (selectedTags != null ? selectedTags.size() : "NULL"));

                    if (selectedTags != null) {
                        TagDebugLogger.logListState("BEFORE_TAGS_UPDATE", noteId, etiquetasNota, etiquetasNotaIds);
                        etiquetasNota = selectedTags;
                        convertTagNamesToIds(selectedTags);
                        updateSelectedTagsDisplay();
                        TagDebugLogger.logListState("AFTER_TAGS_UPDATE", noteId, etiquetasNota, etiquetasNotaIds);
                    } else {
                        TagDebugLogger.logError("TAGS_RESULT_NULL", noteId, "Selected tags is null");
                    }
                } else {
                    TagDebugLogger.logError("TAGS_RESULT_FAILED", noteId, "Result not OK or data is null");
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_note);

        // Log crítico para rastrear el inicio de la actividad
        TagDebugLogger.logCritical("ACTIVITY_CREATE_START - DetailNoteActivity initialized");

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

        // CRÍTICO: Log antes de inicializar las listas
        etiquetasNota = new ArrayList<>();
        etiquetasNotaIds = new ArrayList<>();
        TagDebugLogger.logCritical("ACTIVITY_CREATE_LISTS_INITIALIZED - Lists created empty");

        boolean esNueva = getIntent().getBooleanExtra("esNueva", false);
        TagDebugLogger.logCritical("ACTIVITY_CREATE_ES_NUEVA: " + esNueva);

        Intent intent = getIntent();
        if (intent != null) {
            String id = intent.getStringExtra("id");
            String titulo = intent.getStringExtra("titulo");
            String contenido = intent.getStringExtra("contenido");
            desdePapelera = intent.getBooleanExtra("desdePapelera", false);
            ArrayList<String> intentTagIds = intent.getStringArrayListExtra("etiquetas");

            // CRÍTICO: Log de los datos recibidos del Intent - ARREGLADO PARA EVITAR CRASH
            String tagIdsInfo = "NULL";
            if (intentTagIds != null) {
                try {
                    // Filtrar elementos null para evitar crash en String.join
                    List<String> safeTagIds = new ArrayList<>();
                    for (String tagId : intentTagIds) {
                        if (tagId != null && !tagId.trim().isEmpty()) {
                            safeTagIds.add(tagId);
                        }
                    }

                    if (!safeTagIds.isEmpty()) {
                        etiquetasNotaIds = new ArrayList<>(safeTagIds);
                        try {
                            TagDebugLogger.logCritical("ACTIVITY_CREATE_TAGS_SET - Loading " + safeTagIds.size() + " tagIds: " + String.join(",", safeTagIds));
                        } catch (Exception e) {
                            TagDebugLogger.logCritical("ACTIVITY_CREATE_TAGS_SET - Loading " + safeTagIds.size() + " tagIds: [LOG_ERROR]");
                        }
                        loadTagNamesFromIds(etiquetasNotaIds);
                    } else {
                        TagDebugLogger.logCritical("ACTIVITY_CREATE_NO_TAGS - All tagIds were null or empty");
                        updateSelectedTagsDisplay();
                    }
                } catch (Exception e) {
                    tagIdsInfo = "Count:" + intentTagIds.size() + " - ERROR:" + e.getMessage();
                }
            }

            TagDebugLogger.logCritical(String.format("ACTIVITY_CREATE_INTENT_DATA - ID:%s | Title:%s | Content:%s | TagIds:%s",
                id != null ? id : "NULL",
                titulo != null ? titulo : "NULL",
                contenido != null ? contenido : "NULL",
                tagIdsInfo));

            txtIdNotaDetailNote.setText(id);
            etTitulo.setText(titulo);
            etContenido.setText(contenido);

            if (intentTagIds != null && !intentTagIds.isEmpty()) {
                // Filtrar elementos null para evitar crashes
                List<String> safeTagIds = new ArrayList<>();
                for (String tagId : intentTagIds) {
                    if (tagId != null && !tagId.trim().isEmpty()) {
                        safeTagIds.add(tagId);
                    }
                }

                if (!safeTagIds.isEmpty()) {
                    etiquetasNotaIds = new ArrayList<>(safeTagIds);
                    try {
                        TagDebugLogger.logCritical("ACTIVITY_CREATE_TAGS_SET - Loading " + safeTagIds.size() + " tagIds: " + String.join(",", safeTagIds));
                    } catch (Exception e) {
                        TagDebugLogger.logCritical("ACTIVITY_CREATE_TAGS_SET - Loading " + safeTagIds.size() + " tagIds: [LOG_ERROR]");
                    }
                    loadTagNamesFromIds(etiquetasNotaIds);
                } else {
                    TagDebugLogger.logCritical("ACTIVITY_CREATE_NO_TAGS - All tagIds were null or empty");
                    updateSelectedTagsDisplay();
                }
            } else {
                TagDebugLogger.logCritical("ACTIVITY_CREATE_NO_TAGS - No tagIds received from intent");
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
            TagDebugLogger.logCritical("ACTIVITY_CREATE_NO_INTENT - Intent is null");
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
        findViewById(R.id.fabUbicacion).setOnClickListener(v -> {
            if (!desdePapelera) {
                if (currentGpsLocation != null && !currentGpsLocation.trim().isEmpty()) {
                    Toast.makeText(this, "Esta nota ya tiene una ubicación guardada", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Popup
                mostrarPopupConfirmacionUbicacion();
            } else {
                Snackbar.make(detailNote, "No es posible agregar ubicación desde la papelera", Snackbar.LENGTH_LONG).show();
            }
        });
        cardLocationContainer = findViewById(R.id.cardLocationContainer);
        txtLocationInfo = findViewById(R.id.txtLocationInfo);
        txtLocationLabel = findViewById(R.id.txtLocationLabel);
        btnRemoveLocation = findViewById(R.id.btnRemoveLocation);
        layoutLocationInfo = findViewById(R.id.layoutLocationInfo);

        // VALIDACIÓN CRÍTICA: Verificar que todos los elementos GPS existan
        if (cardLocationContainer == null || txtLocationInfo == null ||
            txtLocationLabel == null || btnRemoveLocation == null || layoutLocationInfo == null) {
            TagDebugLogger.logCritical("GPS_VIEWS_NULL - Some GPS views are null, disabling GPS functionality");
            // Deshabilitar funcionalidad GPS si los views no existen
            currentGpsLocation = null;
        } else {
            // Solo configurar listeners si los views existen
            layoutLocationInfo.setOnClickListener(v -> {
                if (currentGpsLocation != null && !currentGpsLocation.trim().isEmpty()) {
                    mostrarDetallesUbicacion();
                }
            });
            btnRemoveLocation.setOnClickListener(v -> {
                currentGpsLocation = null;
                cardLocationContainer.setVisibility(View.GONE);
                guardarNotaInmediatamente();
                Toast.makeText(this, "Ubicación eliminada", Toast.LENGTH_SHORT).show();
            });
            cargarUbicacionExistente();
        }

        // Removido: obtenerUbicacionActual() automático
        // Ahora solo se obtiene ubicación cuando el usuario presiona el botón flotante
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solo solicitamos permisos, pero no obtenemos ubicación automáticamente
            // Los permisos se solicitarán cuando el usuario presione el botón de ubicación
        }
    }

    private void updateSelectedTagsDisplay() {
        String noteId = txtIdNotaDetailNote.getText().toString();

        // Log del estado antes de la actualización
        TagDebugLogger.logListState("BEFORE_UPDATE_DISPLAY", noteId, etiquetasNota, etiquetasNotaIds);
        TagDebugLogger.logTagList("UPDATE_DISPLAY_START", noteId, etiquetasNota);

        chipGroupSelectedTags.removeAllViews();

        if (etiquetasNota != null && !etiquetasNota.isEmpty()) {
            chipGroupSelectedTags.setVisibility(View.VISIBLE);

            for (String tagName : etiquetasNota) {
                Chip chip = createFilterChip(tagName);
                chipGroupSelectedTags.addView(chip);
            }
        } else {
            chipGroupSelectedTags.setVisibility(View.GONE);
            // Log crítico cuando las etiquetas están vacías
            if (etiquetasNotaIds != null && !etiquetasNotaIds.isEmpty()) {
                TagDebugLogger.logDataInconsistency(noteId,
                    "TagIDs exist but TagNames empty",
                    "IDs:" + etiquetasNotaIds.size() + " Names:" + (etiquetasNota != null ? etiquetasNota.size() : 0));
            }
        }

        TagDebugLogger.logTagList("UPDATE_DISPLAY_END", noteId, etiquetasNota);
        TagDebugLogger.logListState("AFTER_UPDATE_DISPLAY", noteId, etiquetasNota, etiquetasNotaIds);
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
            String noteId = txtIdNotaDetailNote.getText().toString();
            TagDebugLogger.logTagOperation("REMOVE_TAG_START", noteId, "Removing tag: " + tagName);
            TagDebugLogger.logTagList("REMOVE_TAG_BEFORE", noteId, etiquetasNota);

            try {
                // Validacion
                if (etiquetasNota == null || etiquetasNotaIds == null) {
                    TagDebugLogger.logError("REMOVE_TAG_NULL_LISTS", noteId, "Lists are null");
                    Toast.makeText(DetailNoteActivity.this, "Error: Listas de etiquetas no inicializadas", Toast.LENGTH_SHORT).show();
                    return;
                }

                int index = etiquetasNota.indexOf(tagName);
                TagDebugLogger.logTagOperation("REMOVE_TAG_INDEX", noteId, "Tag index: " + index);

                if (index != -1 && index < etiquetasNota.size() && index < etiquetasNotaIds.size()) {
                    String removedTagId = etiquetasNotaIds.get(index);
                    TagDebugLogger.logTagOperation("REMOVE_TAG_VALID", noteId, "Removing tagId: " + removedTagId);

                    etiquetasNota.remove(index);
                    etiquetasNotaIds.remove(index);

                    TagDebugLogger.logTagList("REMOVE_TAG_AFTER", noteId, etiquetasNota);

                    updateSelectedTagsDisplay();
                    guardarNotaInmediatamente();
                    // ForzarActualizacion
                    GenerarData.getInstancia().forceSyncData();
                    Toast.makeText(DetailNoteActivity.this, "Etiqueta eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    TagDebugLogger.logError("REMOVE_TAG_INVALID_INDEX", noteId, "Invalid index or size mismatch");
                    Toast.makeText(DetailNoteActivity.this, "Recargando etiquetas...", Toast.LENGTH_SHORT).show();
                    recargarEtiquetasDesdeFirebase();
                }
            } catch (Exception e) {
                TagDebugLogger.logError("REMOVE_TAG_EXCEPTION", noteId, e.getMessage());
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

        TagDebugLogger.logFirebaseOperation("SAVE_START", id, "Saving with tagIds: " + etiquetasNotaIds.size());
        TagDebugLogger.logTagList("SAVE_BEFORE_FB", id, etiquetasNota);

        if (titulo.isEmpty() && contenido.isEmpty()) {
            return;
        }
        Note nota = new Note();
        nota.setId(id.isEmpty() ? null : id);
        nota.setTitulo(titulo);
        nota.setContenido(contenido);
        nota.setTagIds(new ArrayList<>(etiquetasNotaIds));
        nota.setTimestamp(System.currentTimeMillis());

        // GPS
        if (currentGpsLocation != null) {
            nota.setGpsLocation(currentGpsLocation);
        }

        generarData.getFirestoreRepository().saveNote(nota, new FirestoreRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    TagDebugLogger.logFirebaseOperation("SAVE_SUCCESS", id, "Note saved successfully");
                    if (nota.getId() != null && !nota.getId().isEmpty()) {
                        txtIdNotaDetailNote.setText(nota.getId());
                    }
                    generarData.updateNoteInLocalList(nota);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    TagDebugLogger.logError("SAVE_FIREBASE", id, error);
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
            // CRÍTICO: Evitar guardado automático si las listas están inconsistentes
            String id = txtIdNotaDetailNote.getText().toString();
            String titulo = etTitulo.getText().toString();
            String contenido = etContenido.getText().toString();

            // Solo guardar si hay contenido Y las etiquetas están en estado consistente
            boolean hasContent = !titulo.trim().isEmpty() || !contenido.trim().isEmpty();
            boolean tagsConsistent = (etiquetasNota != null && etiquetasNotaIds != null &&
                                     etiquetasNota.size() == etiquetasNotaIds.size());

            TagDebugLogger.logFirebaseOperation("SAVE_ON_PAUSE_CHECK", id,
                String.format("HasContent:%b TagsConsistent:%b Names:%d IDs:%d",
                hasContent, tagsConsistent,
                etiquetasNota != null ? etiquetasNota.size() : -1,
                etiquetasNotaIds != null ? etiquetasNotaIds.size() : -1));

            // Solo proceder con el guardado si el estado es consistente
            if (hasContent && tagsConsistent) {
                guardarNota();
            } else {
                TagDebugLogger.logFirebaseOperation("SAVE_ON_PAUSE_SKIPPED", id,
                    "Skipping save to prevent data corruption");
            }
        }
    }
    private void guardarNota() {
        String id = txtIdNotaDetailNote.getText().toString();
        String titulo = etTitulo.getText().toString();
        String contenido = etContenido.getText().toString();

        // Log para identificar cuál método está causando el problema
        TagDebugLogger.logFirebaseOperation("SAVE_ON_PAUSE_START", id, "guardarNota() called");
        TagDebugLogger.logListState("SAVE_ON_PAUSE_BEFORE", id, etiquetasNota, etiquetasNotaIds);

        if (titulo.trim().isEmpty() && contenido.trim().isEmpty()) {
            return;
        }
        GenerarData generarData = GenerarData.getInstancia();

        if (id == null || id.isEmpty()) {
            Note nuevaNota = new Note(titulo, contenido);
            if (etiquetasNotaIds != null) {
                nuevaNota.setTagIds(etiquetasNotaIds);
            }
            TagDebugLogger.logFirebaseOperation("SAVE_ON_PAUSE_NEW", id, "Creating new note with " + etiquetasNotaIds.size() + " tags");
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
            // PROBLEMA IDENTIFICADO: Este método está obteniendo la nota desde Firebase
            // pero esa nota puede tener TagIds obsoletos
            TagDebugLogger.logFirebaseOperation("SAVE_ON_PAUSE_UPDATE", id, "Updating existing note - POTENTIAL CONFLICT");

            // SOLUCIÓN: Usar directamente los TagIds actuales en lugar de obtener desde Firebase
            Note notaActualizada = new Note();
            notaActualizada.setId(id);
            notaActualizada.setTitulo(titulo);
            notaActualizada.setContenido(contenido);
            notaActualizada.setTagIds(etiquetasNotaIds != null ? new ArrayList<>(etiquetasNotaIds) : new ArrayList<>());
            notaActualizada.setTimestamp(System.currentTimeMillis());

            // Preservar ubicación GPS si existe
            if (currentGpsLocation != null) {
                notaActualizada.setGpsLocation(currentGpsLocation);
            }

            TagDebugLogger.logFirebaseOperation("SAVE_ON_PAUSE_FIXED", id, "Using current TagIds: " + etiquetasNotaIds.size());

            generarData.getFirestoreRepository().saveNote(notaActualizada, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    TagDebugLogger.logFirebaseOperation("SAVE_ON_PAUSE_SUCCESS", id, "Note saved successfully");
                }
                @Override
                public void onError(String error) {
                    TagDebugLogger.logError("SAVE_ON_PAUSE_ERROR", id, error);
                    Toast.makeText(DetailNoteActivity.this, "Error al actualizar nota: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void loadTagNamesFromIds(ArrayList<String> tagIds) {
        String noteId = txtIdNotaDetailNote.getText().toString();
        TagDebugLogger.logTagOperation("LOAD_TAGS_START", noteId, "TagIds received: " + (tagIds != null ? tagIds.size() : "NULL"));

        if (tagIds == null || tagIds.isEmpty()) {
            TagDebugLogger.logTagOperation("LOAD_TAGS_EMPTY", noteId, "No tags to load, clearing lists");
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
                    TagDebugLogger.logTagOperation("LOAD_TAGS_SUCCESS", noteId, "Tags loaded from DB: " + tags.size());
                    etiquetasNota = new ArrayList<>();
                    etiquetasNotaIds = new ArrayList<>(tagIds); //MantenerID
                    for (Tag tag : tags) {
                        etiquetasNota.add(tag.getEtiquetaDescripcion());
                    }
                    TagDebugLogger.logTagList("LOAD_TAGS_FINAL", noteId, etiquetasNota);
                    updateSelectedTagsDisplay();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    TagDebugLogger.logError("LOAD_TAGS_ERROR", noteId, error);
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
    private void mostrarPopupConfirmacionUbicacion() {
        LocationConfirmationDialog dialog = new LocationConfirmationDialog(this, new LocationConfirmationDialog.LocationConfirmationListener() {
            @Override
            public void onConfirmLocation() {
                // Confirmacion
                if (LocationHelper.hasLocationPermissions(DetailNoteActivity.this)) {
                    if (LocationHelper.isLocationEnabled(DetailNoteActivity.this)) {
                        obtenerUbicacionActual();
                    } else {
                        Toast.makeText(DetailNoteActivity.this, "Por favor habilita el GPS en configuración", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // SolicitarPermisos
                    ActivityCompat.requestPermissions(DetailNoteActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
            @Override
            public void onCancel() {
            }
        });
        dialog.show();
    }
    private void obtenerUbicacionActual() {
        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(String coordinates) {
                // UbicacionObtenida
                currentGpsLocation = coordinates;
                actualizarVistaUbicacion();
                guardarNotaConUbicacion();
                Toast.makeText(DetailNoteActivity.this, "¡Ubicación guardada como recuerdo!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onLocationError(String error) {
                Toast.makeText(DetailNoteActivity.this, "Error obteniendo ubicación: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void actualizarVistaUbicacion() {
        if (currentGpsLocation != null && !currentGpsLocation.trim().isEmpty()) {
            String readableLocation = LocationHelper.getReadableLocation(currentGpsLocation);
            txtLocationInfo.setText(readableLocation);

            // Determinar si es una nota nueva o existente para mostrar el texto correcto
            String noteId = txtIdNotaDetailNote.getText().toString();
            boolean esNotaNueva = (noteId == null || noteId.isEmpty());

            // Cambiar el texto del label según el contexto
            if (esNotaNueva) {
                txtLocationLabel.setText("Aquí estoy:");
            } else {
                txtLocationLabel.setText("Aquí estuve:");
            }

            cardLocationContainer.setVisibility(View.VISIBLE);
        } else {
            cardLocationContainer.setVisibility(View.GONE);
        }
    }
    private void guardarNotaConUbicacion() {
        GenerarData generarData = GenerarData.getInstancia();
        String id = txtIdNotaDetailNote.getText().toString();
        String titulo = etTitulo.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();
        Note nota = new Note();
        nota.setId(id.isEmpty() ? null : id);
        nota.setTitulo(titulo);
        nota.setContenido(contenido);
        nota.setTagIds(new ArrayList<>(etiquetasNotaIds));
        nota.setTimestamp(System.currentTimeMillis());
        nota.setGpsLocation(currentGpsLocation);
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
                    Toast.makeText(DetailNoteActivity.this, "Error guardando nota con ubicación", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void mostrarDetallesUbicacion() {
        if (currentGpsLocation == null || currentGpsLocation.trim().isEmpty()) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        LocationDetailsDialog dialog = new LocationDetailsDialog(this, currentGpsLocation, timestamp, new LocationDetailsDialog.LocationDetailsListener() {
            @Override
            public void onDeleteLocation() {
                currentGpsLocation = null;
                cardLocationContainer.setVisibility(View.GONE);
                guardarNotaInmediatamente();
                Toast.makeText(DetailNoteActivity.this, "Ubicación eliminada", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onClose() {
            }
        });
        dialog.show();
    }

    private void cargarUbicacionExistente() {
        String noteId = txtIdNotaDetailNote.getText().toString();
        if (noteId == null || noteId.isEmpty()) {
            return;
        }
        GenerarData generarData = GenerarData.getInstancia();
        generarData.getFirestoreRepository().getNoteById(noteId, new FirestoreRepository.DataCallback<Note>() {
            @Override
            public void onSuccess(Note note) {
                runOnUiThread(() -> {
                    if (note != null && note.getGpsLocation() != null && !note.getGpsLocation().trim().isEmpty()) {
                        currentGpsLocation = note.getGpsLocation();
                        actualizarVistaUbicacion();
                    } else {
                        cardLocationContainer.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    cardLocationContainer.setVisibility(View.GONE);
                });
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (LocationHelper.isLocationEnabled(this)) {
                    obtenerUbicacionActual();
                } else {
                    Toast.makeText(this, "Por favor habilita el GPS en configuración", Toast.LENGTH_LONG).show();
                }
            } else {
                // Denegar
                Toast.makeText(this, "Por favor conceder permisos...", Toast.LENGTH_LONG).show();
            }
        }
    }
}
