package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;

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

    //VariablesUI
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // VariablesDeAuth
    private FirebaseAuth mAuth;

    // Fragmentos
    private final NoteFragment notes = new NoteFragment();
    private final TagFragment tags = new TagFragment();
    private final TrashFragment trash = new TrashFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        inicializarAutenticacion();
        verificarUsuarioAutenticado();
        inicializarComponentes();
        configurarUI();
        cargarFragmentoInicial();
    }

    private void inicializarAutenticacion() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void verificarUsuarioAutenticado() {
        if (mAuth.getCurrentUser() == null) {
            redirigirALogin();
        }
    }

    private void redirigirALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void inicializarComponentes() {
        GenerarData generarData = GenerarData.getInstancia();
        generarData.clearUserData();
        generarData.initializeWithContext(this);
    }

    private void configurarUI() {
        configurarToolbarYNavegacion();
        configurarPerfilUsuario();
        configurarWindowInsets();
    }

    private void cargarFragmentoInicial() {
        loadFragment(notes);
    }

    private void configurarToolbarYNavegacion() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            manejarSeleccionNavegacion(item.getItemId());
            return true;
        });
    }

    private void manejarSeleccionNavegacion(int itemId) {
        drawerLayout.closeDrawer(GravityCompat.START);
        new Handler(Looper.getMainLooper()).postDelayed(() -> navegarSegunSeleccion(itemId), 300);
    }

    private void navegarSegunSeleccion(int itemId) {
        if (itemId == R.id.notes) {
            loadFragment(notes);
        } else if (itemId == R.id.tags) {
            loadFragment(tags);
        } else if (itemId == R.id.trash) {
            loadFragment(trash);
        } else if (itemId == R.id.btnLogout) {
            cerrarSesion();
        }
    }

    private void configurarPerfilUsuario() {
        setupUserProfile();
    }

    private void configurarWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cerrarSesion() {
        GenerarData generarData = GenerarData.getInstancia();

        if (generarData.getFirestoreRepository() != null) {
            Log.d("MainActivity", "Guardando datos antes del logout...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> finalizarCierreSesion(generarData), 1000);
        } else {
            finalizarCierreSesion(generarData);
        }
    }

    private void finalizarCierreSesion(GenerarData generarData) {
        generarData.clearUserData();
        mAuth.signOut();
        redirigirALogin();
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
        Log.d("MainActivity", "App pausada - forzando guardado de datos");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "MainActivity destruida - limpiando recursos");
        GenerarData generarData = GenerarData.getInstancia();
        if (generarData.getFirestoreRepository() != null) {
            generarData.getFirestoreRepository().cleanup();
        }
    }

    private void setupUserProfile() {
        View headerView = navigationView.getHeaderView(0);
        ImageView imgProfileIcon = headerView.findViewById(R.id.imgProfileIcon);
        ImageView imgEditIcon = headerView.findViewById(R.id.imgEditIcon);
        TextView txtUserName = headerView.findViewById(R.id.txtUserName);
        TextView txtUserEmail = headerView.findViewById(R.id.txtUserEmail);
        updateUserProfileDisplay(imgProfileIcon, txtUserName, txtUserEmail);
        imgProfileIcon.setOnClickListener(v -> {
            imgEditIcon.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                imgEditIcon.setVisibility(View.GONE);
            }, 2000);
        });

        imgEditIcon.setOnClickListener(v -> showProfileIconDialog(imgProfileIcon));
        headerView.findViewById(R.id.cardProfileIcon).setOnClickListener(v ->
                showProfileIconDialog(imgProfileIcon));
    }

    private void updateUserProfileDisplay(ImageView imgProfileIcon, TextView txtUserName, TextView txtUserEmail) {
        UserProfileManager profileManager = new UserProfileManager(this);
        UserProfile profile = profileManager.getUserProfile();
        imgProfileIcon.setImageResource(profile.getProfileIcon());
        txtUserName.setText(profile.getFullName());
        txtUserEmail.setText(profile.getEmail());
    }

    private void loadUserProfile(ImageView imgProfileIcon, TextView txtUserName, TextView txtUserEmail) {
        updateUserProfileDisplay(imgProfileIcon, txtUserName, txtUserEmail);
    }

    private void showProfileIconDialog(ImageView imgProfileIcon) {
        ProfileIconDialog.showIconSelectionDialog(this, selectedIcon -> {
            UserProfileManager profileManager = new UserProfileManager(this);
            UserProfile profile = profileManager.getUserProfile();
            profile.setProfileIcon(selectedIcon);
            profileManager.saveUserProfile(profile);
            imgProfileIcon.setImageResource(selectedIcon);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "MainActivity resumida - verificando datos del usuario");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            View headerView = navigationView.getHeaderView(0);
            ImageView imgProfileIcon = headerView.findViewById(R.id.imgProfileIcon);
            TextView txtUserName = headerView.findViewById(R.id.txtUserName);
            TextView txtUserEmail = headerView.findViewById(R.id.txtUserEmail);
            updateUserProfileDisplay(imgProfileIcon, txtUserName, txtUserEmail);
            GenerarData generarData = GenerarData.getInstancia();

            if (generarData.shouldRefreshData()) {
                Log.d("MainActivity", "Actualizando datos para usuario: " + currentUser.getUid());
                generarData.refreshDataIfNeeded(this);
            }

            if (notes != null && !generarData.hasDataChangeListener(notes)) {
                generarData.addDataChangeListener(notes);
            }
            if (tags != null && !generarData.hasDataChangeListener(tags)) {
                generarData.addDataChangeListener(tags);
            }

        } else {
            Log.w("MainActivity", "No hay usuario autenticado en onResume - redirigiendo al login");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
