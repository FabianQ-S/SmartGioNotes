package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.sgionotes.R;
import com.sgionotes.dialogs.ProfileIconDialog;
import com.sgionotes.fragments.NoteFragment;
import com.sgionotes.fragments.NotePrivateFragment;
import com.sgionotes.fragments.TagFragment;
import com.sgionotes.fragments.TrashFragment;
import com.sgionotes.models.GenerarData;
import com.sgionotes.models.UserProfile;
import com.sgionotes.repository.FirestoreRepository;
import com.sgionotes.utils.UserProfileManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;
    private Handler saveHandler;
    private Runnable saveRunnable;

    private NoteFragment notes = new NoteFragment();
    private TagFragment tags = new TagFragment();
    private TrashFragment trash = new TrashFragment();
    private NotePrivateFragment tagsPrivate = new NotePrivateFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // FirebaseRepository
        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = new FirestoreRepository(this);

        // GenerarDataSqlite
        GenerarData generarData = GenerarData.getInstancia();
        generarData.initializeWithContext(this);
        setupAutoSave();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        loadFragment(notes);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            drawerLayout.closeDrawer(GravityCompat.START);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (id == R.id.notes) {
                    loadFragment(notes);
                } else if (id == R.id.tags) {
                    loadFragment(tags);
                } else if (id == R.id.trash) {
                    loadFragment(trash);
                }
                else if (id == R.id.btnLogout) {
                    logoutUser();
                }
//                else if (id == R.id.tags_private) {
//                    loadFragment(tagsPrivate);
//                }
            }, 300);

            return true;
        });
        setupUserProfile();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void setupAutoSave() {
        // Ya no necesitamos auto-guardado manual porque Firestore maneja la persistencia automáticamente
        // El código se mantiene para compatibilidad pero no hace nada
    }

    private void saveUserDataToFirestore(Runnable onComplete) {
        // Ya no necesitamos guardar manualmente porque Firestore maneja la persistencia
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private void saveUserDataToFirestore() {
        // Método mantenido para compatibilidad
    }

    private void logoutUser() {
        // SessionOff - Ya no necesitamos guardar manualmente
        mAuth.signOut();

        // LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor, fragment)
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // GuardarAlSuspederSesion
        saveUserDataToFirestore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (saveHandler != null && saveRunnable != null) {
            saveHandler.removeCallbacks(saveRunnable);
        }
        saveUserDataToFirestore();
    }

    private void setupUserProfile() {
        View headerView = navigationView.getHeaderView(0);
        ImageView imgProfileIcon = headerView.findViewById(R.id.imgProfileIcon);
        ImageView imgEditIcon = headerView.findViewById(R.id.imgEditIcon);
        TextView txtUserName = headerView.findViewById(R.id.txtUserName);
        TextView txtUserEmail = headerView.findViewById(R.id.txtUserEmail);

        loadUserProfile(imgProfileIcon, txtUserName, txtUserEmail);

        imgProfileIcon.setOnClickListener(v -> {
            imgEditIcon.setVisibility(View.VISIBLE);

            // OcultarAutomáticamente
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                imgEditIcon.setVisibility(View.GONE);
            }, 2000);
        });

        // Editar
        imgEditIcon.setOnClickListener(v -> showProfileIconDialog(imgProfileIcon, txtUserName, txtUserEmail));
        headerView.findViewById(R.id.cardProfileIcon).setOnClickListener(v ->
                showProfileIconDialog(imgProfileIcon, txtUserName, txtUserEmail));
    }

    private void loadUserProfile(ImageView imgProfileIcon, TextView txtUserName, TextView txtUserEmail) {
        UserProfileManager profileManager = new UserProfileManager(this);
        UserProfile profile = profileManager.getUserProfile();
        imgProfileIcon.setImageResource(profile.getProfileIcon());
        txtUserName.setText(profile.getFullName());
        String email = profile.getEmail();

        if (email.isEmpty() && mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
            profile.setEmail(email);
            profileManager.saveUserProfile(profile);
        }
        txtUserEmail.setText(email);
    }

    private void showProfileIconDialog(ImageView imgProfileIcon, TextView txtUserName, TextView txtUserEmail) {
        ProfileIconDialog.showIconSelectionDialog(this, selectedIcon -> {
            UserProfileManager profileManager = new UserProfileManager(this);
            UserProfile profile = profileManager.getUserProfile();
            profile.setProfileIcon(selectedIcon);
            profileManager.saveUserProfile(profile);
            imgProfileIcon.setImageResource(selectedIcon);
        });
    }
}