package com.sgionotes.activities;

import android.Manifest;
import android.app.Activity;
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
import java.util.ArrayList;
import java.util.List;

public class DetailNoteActivity extends AppCompatActivity {

    private boolean estáGuardando = false;
    private boolean notaYaCreada = false;
    private long ultimoGuardado = 0;
    private static final long INTERVALO_MINIMO_GUARDADO = 2000;
    private ArrayList<String> etiquetasNota;
    private ArrayList<String> etiquetasNotaIds;
    private TextView txtIdNotaDetailNote;
    private EditText etTitulo;
    private EditText etContenido;
    private LinearLayout detailNote;
    private ChipGroup chipGroupSelectedTags;
    private boolean desdePapelera = false;
    private boolean enviandoAPapelera = false;
    private String noteId;

    // VariablesGPS
    private CardView cardLocationContainer;
    private TextView txtLocationInfo;
    private TextView txtLocationLabel;
    private ImageButton btnRemoveLocation;
    private LinearLayout layoutLocationInfo;
    private String currentGpsLocation = null;

    // Constantes
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final ActivityResultLauncher<Intent> tagsLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                ArrayList<String> returnedTagIds = result.getData().getStringArrayListExtra("selectedTags");
                if (returnedTagIds != null) {
                    etiquetasNotaIds.clear();
                    etiquetasNotaIds.addAll(returnedTagIds);
                    cargarEtiquetasPorIds(etiquetasNotaIds);
                }
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_note);
        inicializarUI();
        configurarToolbar();
        inicializarListas();
        procesarIntent();
        configurarListeners();
        configurarUbicacion();
        configurarPermisos();
    }

    private void inicializarUI() {
        txtIdNotaDetailNote = findViewById(R.id.txtIdNotaDetailNote);
        etTitulo = findViewById(R.id.etTitulo);
        etContenido = findViewById(R.id.etmDetalleNota);
        detailNote = findViewById(R.id.detailNote);
        chipGroupSelectedTags = findViewById(R.id.chipGroupSelectedTags);
        cardLocationContainer = findViewById(R.id.cardLocationContainer);
        txtLocationInfo = findViewById(R.id.txtLocationInfo);
        txtLocationLabel = findViewById(R.id.txtLocationLabel);
        btnRemoveLocation = findViewById(R.id.btnRemoveLocation);
        layoutLocationInfo = findViewById(R.id.layoutLocationInfo);
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
    private void inicializarListas() {
        etiquetasNota = new ArrayList<>();
        etiquetasNotaIds = new ArrayList<>();
    }
    private void procesarIntent() {
        boolean esNueva = getIntent().getBooleanExtra("esNueva", false);
        Intent intent = getIntent();

        if (intent != null) {
            procesarDatosIntent(intent, esNueva);
            actualizarChipsEtiquetas();
        } else {
            actualizarChipsEtiquetas();
        }

        configurarVisibilidadBotones(esNueva, desdePapelera);
        configurarWindowInsets();
    }
    private void procesarDatosIntent(Intent intent, boolean esNueva) {
        String id = intent.getStringExtra("id");
        String titulo = intent.getStringExtra("titulo");
        String contenido = intent.getStringExtra("contenido");
        desdePapelera = intent.getBooleanExtra("desdePapelera", false);
        ArrayList<String> intentTagIds = intent.getStringArrayListExtra("etiquetas");

        noteId = id;
        if (id != null && !id.trim().isEmpty()) {
            txtIdNotaDetailNote.setText(id);
            notaYaCreada = true; 
        }
        if (titulo != null) etTitulo.setText(titulo);
        if (contenido != null) etContenido.setText(contenido);

        if (intentTagIds != null) {
            procesarEtiquetasIntent(intentTagIds, esNueva);
        }
    }
    private void procesarEtiquetasIntent(ArrayList<String> intentTagIds, boolean esNueva) {
        try {
            List<String> safeTagIds = filtrarTagIdsValidos(intentTagIds);

            if (!safeTagIds.isEmpty()) {
                etiquetasNotaIds.clear();
                etiquetasNotaIds.addAll(safeTagIds);
                cargarEtiquetasPorIds(etiquetasNotaIds);
            }
        } catch (Exception e) {
        }
    }

    private List<String> filtrarTagIdsValidos(ArrayList<String> tagIds) {
        List<String> safeTagIds = new ArrayList<>();
        for (String tagId : tagIds) {
            if (tagId != null && !tagId.trim().isEmpty()) {
                safeTagIds.add(tagId);
            }
        }
        return safeTagIds;
    }

    private void configurarWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailNote), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configurarListeners() {
        findViewById(R.id.fabEtiquetas).setOnClickListener(v -> manejarClickEtiquetas());
        findViewById(R.id.fabEliminar).setOnClickListener(v -> manejarClickEliminar());
        findViewById(R.id.fabUbicacion).setOnClickListener(v -> manejarClickUbicacion());
    }

    private void manejarClickEtiquetas() {
        if (!desdePapelera) {
            Intent intentTags = new Intent(DetailNoteActivity.this, TagsActivity.class);
            if (etiquetasNotaIds != null) {
                intentTags.putStringArrayListExtra("tags", etiquetasNotaIds);
            }
            tagsLauncher.launch(intentTags);
        } else {
            mostrarMensajePapelera("No es posible editar etiquetas desde la papelera");
        }
    }

    private void manejarClickEliminar() {
        if (desdePapelera) {
            mostrarDialogoEliminarNotaIndividual(txtIdNotaDetailNote.getText().toString());
        } else {
            mostrarDialogoEnviarPapelera();
        }
    }

    private void manejarClickUbicacion() {
        if (!desdePapelera) {
            if (currentGpsLocation != null && !currentGpsLocation.trim().isEmpty()) {
                Toast.makeText(this, "Esta nota ya tiene una ubicación guardada", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarPopupConfirmacionUbicacion();
        } else {
            mostrarMensajePapelera("No es posible agregar ubicación desde la papelera");
        }
    }

    private void mostrarMensajePapelera(String mensaje) {
        Snackbar.make(detailNote, mensaje, Snackbar.LENGTH_LONG).show();
    }

    private void configurarUbicacion() {
        if (!validarElementosGPS()) {
            currentGpsLocation = null;
            return;
        }

        layoutLocationInfo.setOnClickListener(v -> {
            if (currentGpsLocation != null && !currentGpsLocation.trim().isEmpty()) {
                mostrarDetallesUbicacion();
            }
        });

        btnRemoveLocation.setOnClickListener(v -> eliminarUbicacion());
        cargarUbicacionExistente();
    }

    private boolean validarElementosGPS() {
        return cardLocationContainer != null && txtLocationInfo != null &&
               txtLocationLabel != null && btnRemoveLocation != null && layoutLocationInfo != null;
    }

    private void eliminarUbicacion() {
        currentGpsLocation = null;
        cardLocationContainer.setVisibility(View.GONE);
        guardarNotaUnificado();
        Toast.makeText(this, "Ubicación eliminada", Toast.LENGTH_SHORT).show();
    }

    private void configurarPermisos() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        }
    }

    private void actualizarChipsEtiquetas() {
        chipGroupSelectedTags.removeAllViews();

        if (etiquetasNota != null && !etiquetasNota.isEmpty()) {
            chipGroupSelectedTags.setVisibility(View.VISIBLE);

            for (String tagName : etiquetasNota) {
                Chip chip = createFilterChip(tagName);
                chipGroupSelectedTags.addView(chip);
            }
        } else {
            chipGroupSelectedTags.setVisibility(View.GONE);
            if (etiquetasNotaIds != null && !etiquetasNotaIds.isEmpty()) {
            }
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
            String noteId = txtIdNotaDetailNote.getText().toString();

            try {
                // Validacion
                if (etiquetasNota == null || etiquetasNotaIds == null) {
                    Toast.makeText(DetailNoteActivity.this, "Error: Listas de etiquetas no inicializadas", Toast.LENGTH_SHORT).show();
                    return;
                }

                int index = etiquetasNota.indexOf(tagName);

                if (index != -1 && index < etiquetasNota.size() && index < etiquetasNotaIds.size()) {
                    etiquetasNota.remove(index);
                    etiquetasNotaIds.remove(index);

                    actualizarChipsEtiquetas();
                    guardarNotaInmediatamente();
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
        // Usar el mismo mecanismo unificado para prevenir duplicaciones
        guardarNotaUnificado();
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
                        cargarEtiquetasPorIds(etiquetasNotaIds);
                    } else {
                        etiquetasNotaIds.clear();
                        etiquetasNota.clear();
                        actualizarChipsEtiquetas();
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
            guardarNotaUnificado();
        }
    }
    private void guardarNotaUnificado() {
    long ahora = System.currentTimeMillis();
    if (estáGuardando) return; // ya hay un guardado en curso
    if ((ahora - ultimoGuardado) < INTERVALO_MINIMO_GUARDADO) return; // debounce

        String id = txtIdNotaDetailNote.getText().toString();
        String titulo = etTitulo.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();

        if (titulo.isEmpty() && contenido.isEmpty()) {
            return;
        }

        estáGuardando = true;
        GenerarData generarData = GenerarData.getInstancia();
        Note nota;

        if (id == null || id.isEmpty()) {
            // Solo crear una nueva nota si realmente no existe aún
            if (notaYaCreada && noteId != null && !noteId.isEmpty()) {
                // Tenemos un ID en memoria pero no en el TextView (caso raro), úsalo para actualizar
                id = noteId;
                nota = new Note();
                nota.setId(id);
                nota.setTitulo(titulo);
                nota.setContenido(contenido);
            } else {
                nota = new Note(titulo, contenido); // creación inicial
            }
        } else {
            nota = new Note();
            nota.setId(id);
            nota.setTitulo(titulo);
            nota.setContenido(contenido);
        }

        if (etiquetasNotaIds != null) {
            nota.setTagIds(new ArrayList<>(etiquetasNotaIds));
        } else {
            nota.setTagIds(new ArrayList<>());
        }

        if (currentGpsLocation != null) {
            nota.setGpsLocation(currentGpsLocation);
        }
        nota.setTimestamp(System.currentTimeMillis());
        generarData.getFirestoreRepository().saveNote(nota, new FirestoreRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (nota.getId() != null && !nota.getId().isEmpty()) {
                        txtIdNotaDetailNote.setText(nota.getId());
                        noteId = nota.getId();
                        notaYaCreada = true;
                    }
                    generarData.updateNoteInLocalList(nota);
                    estáGuardando = false;
                    ultimoGuardado = System.currentTimeMillis();
                });
            }

            @Override
            public void onError(String error) {
                estáGuardando = false;
                runOnUiThread(() -> Toast.makeText(DetailNoteActivity.this, "Error guardando nota: " + error, Toast.LENGTH_SHORT).show());
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
                if (LocationHelper.hasLocationPermissions(DetailNoteActivity.this)) {
                    if (LocationHelper.isLocationEnabled(DetailNoteActivity.this)) {
                        obtenerUbicacionActual();
                    } else {
                        Toast.makeText(DetailNoteActivity.this, "Por favor habilita el GPS en configuración", Toast.LENGTH_LONG).show();
                    }
                } else {
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
                currentGpsLocation = coordinates;
                actualizarVistaUbicacion();
                guardarNotaUnificado();
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

            String noteId = txtIdNotaDetailNote.getText().toString();
            boolean esNotaNueva = (noteId == null || noteId.isEmpty());
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
                guardarNotaUnificado();
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
                Toast.makeText(this, "Por favor conceder permisos...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cargarEtiquetasPorIds(ArrayList<String> tagIds) {
        String noteId = txtIdNotaDetailNote.getText().toString();

        if (tagIds == null || tagIds.isEmpty()) {
            etiquetasNota = new ArrayList<>();
            etiquetasNotaIds = new ArrayList<>();
            actualizarChipsEtiquetas();
            return;
        }
        GenerarData generarData = GenerarData.getInstancia();
        generarData.getFirestoreRepository().getTagsByIds(tagIds, new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> tags) {
                runOnUiThread(() -> {
                    etiquetasNota = new ArrayList<>();
                    etiquetasNotaIds = new ArrayList<>(tagIds); // Mantener IDs originales
                    for (Tag tag : tags) {
                        etiquetasNota.add(tag.getEtiquetaDescripcion());
                    }
                    actualizarChipsEtiquetas();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    etiquetasNota = new ArrayList<>();
                    etiquetasNotaIds = new ArrayList<>();
                    actualizarChipsEtiquetas();
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
                runOnUiThread(() -> actualizarChipsEtiquetas());
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DetailNoteActivity.this, "Error cargando etiquetas", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
