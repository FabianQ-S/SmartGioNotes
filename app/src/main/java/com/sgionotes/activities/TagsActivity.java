package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sgionotes.R;
import com.sgionotes.models.Tag;
import com.sgionotes.models.GenerarData;
import com.sgionotes.repository.FirestoreRepository;
import java.util.ArrayList;
import java.util.List;

public class TagsActivity extends AppCompatActivity {

    private ChipGroup chipGroupFavorites;
    private ChipGroup chipGroupAllTags;
    private TextView tvFavoritesHeader;
    private TextView tvAllTagsHeader;
    private List<Tag> allTags;
    private ArrayList<String> selectedTagsFromNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initializeViews();

        Intent intent = getIntent();
        if (intent != null) {
            selectedTagsFromNote = intent.getStringArrayListExtra("tags");
            if (selectedTagsFromNote == null) {
                selectedTagsFromNote = new ArrayList<>();
            }
        }

        loadAllTags();
    }

    private void initializeViews() {
        chipGroupFavorites = findViewById(R.id.chipGroupFavorites);
        chipGroupAllTags = findViewById(R.id.chipGroupAllTags);
        tvFavoritesHeader = findViewById(R.id.tvFavoritesHeader);
        tvAllTagsHeader = findViewById(R.id.tvAllTagsHeader);
    }

    private void loadAllTags() {
        GenerarData generarData = GenerarData.getInstance();
        generarData.getFirestoreRepository().getAllTags(new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> tags) {
                allTags = tags;
                organizeAndDisplayTags();
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private void organizeAndDisplayTags() {
        if (allTags == null || allTags.isEmpty()) {
            tvFavoritesHeader.setVisibility(View.GONE);
            tvAllTagsHeader.setVisibility(View.GONE);
            return;
        }

        List<Tag> favoriteTags = new ArrayList<>();
        List<Tag> regularTags = new ArrayList<>();

        for (Tag tag : allTags) {
            if (tag.isFavorite()) {
                favoriteTags.add(tag);
            } else {
                regularTags.add(tag);
            }
        }

        // Ordenamiento
        favoriteTags.sort((t1, t2) -> Long.compare(t2.getFavoriteTimestamp(), t1.getFavoriteTimestamp()));
        regularTags.sort((t1, t2) -> t1.getEtiquetaDescripcion().compareToIgnoreCase(t2.getEtiquetaDescripcion()));

        if (!favoriteTags.isEmpty()) {
            tvFavoritesHeader.setVisibility(View.VISIBLE);
            populateChipGroup(chipGroupFavorites, favoriteTags, true);
        } else {
            tvFavoritesHeader.setVisibility(View.GONE);
        }

        if (!regularTags.isEmpty()) {
            tvAllTagsHeader.setVisibility(View.VISIBLE);
            populateChipGroup(chipGroupAllTags, regularTags, false);
        } else {
            tvAllTagsHeader.setVisibility(View.GONE);
        }
    }

    private void populateChipGroup(ChipGroup chipGroup, List<Tag> tags, boolean isFavoriteGroup) {
        chipGroup.removeAllViews();

        for (Tag tag : tags) {
            Chip chip = createChip(tag, isFavoriteGroup);
            chipGroup.addView(chip);
        }
    }

    private Chip createChip(Tag tag, boolean isFavorite) {
        Chip chip = new Chip(this);
        chip.setText(tag.getEtiquetaDescripcion());
        chip.setTextSize(14f);
        chip.setChipBackgroundColorResource(R.color.chipBackground);
        chip.setTextColor(getResources().getColor(R.color.chipText, getTheme()));
        chip.setChipStrokeColorResource(R.color.chipStrokeColor);
        chip.setChipStrokeWidth(2f);

        boolean isSelected = selectedTagsFromNote.contains(tag.getEtiquetaDescripcion());
        chip.setCheckable(true);
        chip.setChecked(isSelected);

        if (isSelected) {
            chip.setChipBackgroundColorResource(R.color.chipSelectedBackground);
            chip.setTextColor(getResources().getColor(R.color.chipSelectedText, getTheme()));
            chip.setCheckedIconVisible(true);
            chip.setCheckedIconResource(android.R.drawable.ic_menu_save);
            chip.setCheckedIconTint(getResources().getColorStateList(R.color.chipSelectedText, getTheme()));
        } else {
            chip.setCheckedIconVisible(false);
        }
        chip.setCloseIconVisible(false);
        chip.setClickable(true);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chip.setChipBackgroundColorResource(R.color.chipSelectedBackground);
                chip.setTextColor(getResources().getColor(R.color.chipSelectedText, getTheme()));
                chip.setCheckedIconVisible(true);
                chip.setCheckedIconTint(getResources().getColorStateList(R.color.chipSelectedText, getTheme()));
                if (!selectedTagsFromNote.contains(tag.getEtiquetaDescripcion())) {
                    selectedTagsFromNote.add(tag.getEtiquetaDescripcion());
                }
            } else {
                // Deseleccionar
                chip.setChipBackgroundColorResource(R.color.chipBackground);
                chip.setTextColor(getResources().getColor(R.color.chipText, getTheme()));
                chip.setCheckedIconVisible(false);
                selectedTagsFromNote.remove(tag.getEtiquetaDescripcion());
            }
        });

        return chip;
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
