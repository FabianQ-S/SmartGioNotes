package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sgionotes.R;

public class DetailNoteActivity extends AppCompatActivity {

    EditText etTitulo;
    EditText etContenido;

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

        Intent intent = getIntent();
        if (intent != null) {
            String titulo = intent.getStringExtra("titulo");
            String contenido = intent.getStringExtra("contenido");
            boolean desdePapelera = intent.getBooleanExtra("desdePapelera", false);

            etTitulo.setText(titulo);
            etContenido.setText(contenido);

            if (desdePapelera) {
                etTitulo.setFocusable(false);
                etTitulo.setClickable(true);
                etContenido.setFocusable(false);
                etContenido.setClickable(true);

                View.OnClickListener mostrarMensaje = v -> {
                    Toast.makeText(this, "No es posible editar notas desde la papelera", Toast.LENGTH_LONG).show();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}