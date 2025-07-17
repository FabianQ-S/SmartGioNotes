package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.sgionotes.R;

import java.util.ArrayList;

public class DetailNoteActivity extends AppCompatActivity {

    EditText etTitulo;
    EditText etContenido;
    LinearLayout detailNote;
    private ArrayList<String> etiquetasNota;

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

        etTitulo = findViewById(R.id.etTitulo);
        etContenido = findViewById(R.id.etmDetalleNota);
        detailNote = findViewById(R.id.detailNote);

        Intent intent = getIntent();
        if (intent != null) {
            String titulo = intent.getStringExtra("titulo");
            String contenido = intent.getStringExtra("contenido");
            boolean desdePapelera = intent.getBooleanExtra("desdePapelera", false);
            etiquetasNota = intent.getStringArrayListExtra("etiquetas");

            etTitulo.setText(titulo);
            etContenido.setText(contenido);

            if (desdePapelera) {
                etTitulo.setFocusable(false);
                etTitulo.setClickable(true);
                etContenido.setFocusable(false);
                etContenido.setClickable(true);

                View.OnClickListener mostrarMensaje = v -> {
                    Snackbar.make(detailNote, "No es posible editar notas desde la papelera", Snackbar.LENGTH_LONG).show();
                };

                etTitulo.setOnClickListener(mostrarMensaje);
                etContenido.setOnClickListener(mostrarMensaje);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailNote), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar botón flotante de etiquetas
        findViewById(R.id.fabEtiquetas).setOnClickListener(v -> {
            Intent intentTags = new Intent(DetailNoteActivity.this, TagsActivity.class);
            if (etiquetasNota != null) {
                intentTags.putStringArrayListExtra("tags", etiquetasNota);
            }
            startActivity(intentTags);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public void onBackPressed() {
        devolverSiEsNuevo();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            devolverSiEsNuevo();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void devolverSiEsNuevo() {
        Intent intent = getIntent();
        boolean esNueva = intent.getBooleanExtra("esNueva", false);

        if (esNueva) {
            String titulo = etTitulo.getText().toString().trim();
            String contenido = etContenido.getText().toString().trim();

            if (!titulo.isEmpty() || !contenido.isEmpty()) {
                Intent data = new Intent();
                data.putExtra("titulo", titulo);
                data.putExtra("contenido", contenido);
                setResult(RESULT_OK, data);
            } else {
                setResult(RESULT_CANCELED);
            }
        }
    }

}