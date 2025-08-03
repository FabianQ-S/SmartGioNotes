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
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;
import com.sgionotes.repository.FirestoreRepository;
import com.sgionotes.utils.UserProfileManager;
import java.util.Objects;
import java.util.List;
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
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        firestoreRepository = new FirestoreRepository(this);
        setupToolbarAndNavigation();
        GenerarData generarData = GenerarData.getInstancia();
        generarData.clearUserData();
        generarData.initializeWithContext(this);
        loadFragment(notes);
        setupUserProfile();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void setupToolbarAndNavigation() {
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
                } else if (id == R.id.btnLogout) {
                    logoutUser();
                }
            }, 300);
            return true;
        });
    }
    private void setupAutoSave() {
    }
    private void saveUserDataToFirestore(Runnable onComplete) {
        if (onComplete != null) {
            onComplete.run();
        }
    }
    private void saveUserDataToFirestore() {
    }
    private void logoutUser() {
        GenerarData generarData = GenerarData.getInstancia();
        if (generarData.getFirestoreRepository() != null) {
            Log.d("MainActivity", "Guardando datos antes del logout...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                generarData.clearUserData();
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 1000); //FirestoreCarga
        } else {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
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
        GenerarData generarData = GenerarData.getInstancia();
        if (generarData.getFirestoreRepository() != null) {
            generarData.getFirestoreRepository().getAllNotes(new FirestoreRepository.DataCallback<List<Note>>() {
                @Override
                public void onSuccess(List<Note> notes) {
                }
                @Override
                public void onError(String error) {
                }
            });
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "MainActivity destruida - limpiando recursos");
        GenerarData generarData = GenerarData.getInstancia();
        if (generarData.getFirestoreRepository() != null) {
            generarData.getFirestoreRepository().cleanup();
        }
        if (saveHandler != null && saveRunnable != null) {
            saveHandler.removeCallbacks(saveRunnable);
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

        imgEditIcon.setOnClickListener(v -> showProfileIconDialog(imgProfileIcon, txtUserName, txtUserEmail));
        headerView.findViewById(R.id.cardProfileIcon).setOnClickListener(v ->
                showProfileIconDialog(imgProfileIcon, txtUserName, txtUserEmail));
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

    private void showProfileIconDialog(ImageView imgProfileIcon, TextView txtUserName, TextView txtUserEmail) {
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
            Log.d("MainActivity", "Forzando reinicialización completa en onResume para usuario: " + currentUser.getUid());
            generarData.forceCompleteReinitialization(this);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (notes != null) {
                    generarData.addDataChangeListener(notes);
                }
                if (tags != null) {
                    generarData.addDataChangeListener(tags);
                }
                generarData.forceUpdateAllFragments();
            }, 1000);

        } else {
            Log.w("MainActivity", "No hay usuario autenticado en onResume - redirigiendo al login");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
