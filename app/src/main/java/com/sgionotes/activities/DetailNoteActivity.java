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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.sgionotes.R;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;
import com.sgionotes.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DetailNoteActivity extends AppCompatActivity {

    private ArrayList<String> etiquetasNota;
    private TextView txtIdNotaDetailNote;
    private EditText etTitulo;
    private EditText etContenido;
    private LinearLayout detailNote;
    private boolean desdePapelera = false;

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

        boolean esNueva = getIntent().getBooleanExtra("esNueva", false);

        Intent intent = getIntent();
        if (intent != null) {
            String id = intent.getStringExtra("id");
            String titulo = intent.getStringExtra("titulo");
            String contenido = intent.getStringExtra("contenido");
            desdePapelera = intent.getBooleanExtra("desdePapelera", false);
            etiquetasNota = intent.getStringArrayListExtra("etiquetas");

            txtIdNotaDetailNote.setText(id);
            etTitulo.setText(titulo);
            etContenido.setText(contenido);

            if (desdePapelera) {
                etTitulo.setFocusable(false);
                etTitulo.setClickable(true);
                etContenido.setFocusable(false);
                etContenido.setClickable(true);

                View.OnClickListener mostrarMensaje = v -> {
                    mostrarDialogoEliminarNotaIndividual(id);
                };

                etTitulo.setOnClickListener(mostrarMensaje);
                etContenido.setOnClickListener(mostrarMensaje);
            }
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
                startActivity(intentTags);
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
            Toast.makeText(this, "Función GPS próximamente", Toast.LENGTH_SHORT).show();
        });
    }
    private void configurarVisibilidadBotones(boolean esNueva, boolean desdePapelera) {
        View fabEtiquetas = findViewById(R.id.fabEtiquetas);
        View fabEliminar = findViewById(R.id.fabEliminar);
        View fabUbicacion = findViewById(R.id.fabUbicacion);

        if (esNueva) {
            fabEtiquetas.setVisibility(View.VISIBLE);
            fabEliminar.setVisibility(View.GONE);
            fabUbicacion.setVisibility(View.GONE);
        } else if (desdePapelera) {
            fabEtiquetas.setVisibility(View.GONE);
            fabEliminar.setVisibility(View.VISIBLE);
            fabUbicacion.setVisibility(View.GONE);
        } else {
            fabEtiquetas.setVisibility(View.VISIBLE);
            fabEliminar.setVisibility(View.VISIBLE);
            fabUbicacion.setVisibility(View.VISIBLE);
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
            guardarNotaAntesDePapelera();

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
                        }, 500); //delay
                    });
                }

                @Override
                public void onError(String error) {
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
        if (etiquetasNota != null) {
            notaActualizada.setTagIds(etiquetasNota);
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
        if (!desdePapelera) {
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
            // Nueva nota
            Note nuevaNota = new Note(titulo, contenido);
            if (etiquetasNota != null) {
                nuevaNota.setTagIds(etiquetasNota);
            }
            generarData.getFirestoreRepository().saveNote(nuevaNota, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    //NotaGuardada
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(DetailNoteActivity.this, "Error al guardar nota: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Note notaActualizada = new Note(titulo, contenido);
            notaActualizada.setId(id);
            if (etiquetasNota != null) {
                notaActualizada.setTagIds(etiquetasNota);
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