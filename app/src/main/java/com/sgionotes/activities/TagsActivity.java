package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sgionotes.R;
import com.sgionotes.adapters.EditableTagAdapter;
import com.sgionotes.models.Tag;
import com.sgionotes.models.GenerarData;
import java.util.ArrayList;
import java.util.List;

public class TagsActivity extends AppCompatActivity implements EditableTagAdapter.OnTagActionListener {

    private RecyclerView recyclerViewTags;
    private EditableTagAdapter tagAdapter;
    private List<Tag> tagsNota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edición de Etiquetas");
        }

        recyclerViewTags = findViewById(R.id.recyclerViewTags);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<String> tagsTexto = intent.getStringArrayListExtra("tags");
            if (tagsTexto != null) {
                tagsNota = new ArrayList<>();
                for (String tagTexto : tagsTexto) {
                    tagsNota.add(new Tag(tagTexto));
                }
                GenerarData.getInstance().loadFavorites(this);
                setupRecyclerView();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        GenerarData.getInstance().saveFavorites(this);
    }

    private void setupRecyclerView() {
        tagAdapter = new EditableTagAdapter(this, tagsNota, this);
        recyclerViewTags.setAdapter(tagAdapter);
    }

    @Override
    public void onTagEdited(int position, String newText) {
        if (position >= 0 && position < tagsNota.size()) {
            tagsNota.get(position).setEtiquetaDescripcion(newText);
            tagAdapter.notifyItemChanged(position);
            Toast.makeText(this, "Etiqueta actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTagDeleted(int position) {
        if (position >= 0 && position < tagsNota.size()) {
            String tagName = tagsNota.get(position).getEtiquetaDescripcion();
            tagsNota.remove(position);
            tagAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Etiqueta '" + tagName + "' eliminada", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTagAddedToFavorites(int position) {
        if (position >= 0 && position < tagsNota.size()) {
            String tagName = tagsNota.get(position).getEtiquetaDescripcion();
            Toast.makeText(this, "'" + tagName + "' añadida a favoritas", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
