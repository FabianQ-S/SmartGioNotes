package com.sgionotes;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    NoteFragment notes = new NoteFragment();
    TagFragment tags = new TagFragment();
    TrashFragment trash = new TrashFragment();
    BottomNavigationView navigationView;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        navigationView = findViewById(R.id.navMenu);
        floatingActionButton = findViewById(R.id.addNota);

        loadFragment(notes);

        navigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.notes) {
                loadFragment(notes);
                return true;
            }
            else if (item.getItemId()== R.id.tags) {
                loadFragment(tags);
                return true;
            }
            else if (item.getItemId() == R.id.trash) {
                loadFragment(trash);
                return true;
            }
            return false;
        });

        floatingActionButton.setOnClickListener(btn -> {
            Intent intent = new Intent(MainActivity.this, DetailNote.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor, fragment)
                .commit();
    }
}