package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sgionotes.R;
import com.sgionotes.models.Tag;
import java.util.ArrayList;
import java.util.List;

public class TagsActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
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

        chipGroup = findViewById(R.id.chipGroup);

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<String> tagsTexto = intent.getStringArrayListExtra("tags");
            if (tagsTexto != null) {
                tagsNota = new ArrayList<>();
                for (String tagTexto : tagsTexto) {
                    tagsNota.add(new Tag(tagTexto));
                }
                mostrarChips();
            }
        }
    }

    private void mostrarChips() {
        for (Tag tag : tagsNota) {
            Chip chip = new Chip(this);
            chip.setText(tag.getEtiquetaDescripcion());
            chip.setChipBackgroundColorResource(R.color.chipBackground);
            chip.setTextColor(getResources().getColor(R.color.chipText));
            chip.setTextSize(14);
            chipGroup.addView(chip);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
