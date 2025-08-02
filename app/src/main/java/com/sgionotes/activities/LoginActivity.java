package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sgionotes.R;
import com.sgionotes.repository.FirestoreRepository;
import com.sgionotes.models.GenerarData;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView registerRedirect;
    private EditText txtEmail;
    private EditText txtPassword;
    private LinearLayout loginMain;

    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;
    boolean registroExitoso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // InicializarFirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = new FirestoreRepository(this);

        registroExitoso = getIntent().getBooleanExtra("registro_exitoso", false);

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginMain = findViewById(R.id.loginMain);

        if (registroExitoso) {
            Snackbar.make(loginMain, "Cuenta creada. Inicia sesión con tus credenciales", Snackbar.LENGTH_LONG).show();
        }

        btnLogin.setOnClickListener(btn -> {
            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(loginMain, "Completa los campos para acceder", Snackbar.LENGTH_LONG).show();
                return;
            }

            //Loanding
            btnLogin.setEnabled(false);
            btnLogin.setText("Iniciando sesión...");

            loginWithFirebase(email, password);
        });

        registerRedirect = findViewById(R.id.registerRedirect);
        registerRedirect.setOnClickListener(reg -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loginWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login
                        FirebaseUser user = mAuth.getCurrentUser();
                        Snackbar.make(loginMain, "Bienvenido " + user.getEmail(), Snackbar.LENGTH_SHORT).show();
                        loadUserDataAndNavigate();
                    } else {
                        // LoginFallido
                        Snackbar.make(loginMain, "Usuario y/o contraseña inválidos", Snackbar.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Iniciar Sesión");
                    }
                });
    }

    private void loadUserDataAndNavigate() {
        GenerarData generarData = GenerarData.getInstancia();
        generarData.initializeWithContext(this);

        // FirestoneBackupRestore
        firestoreRepository.restoreDataFromFirestore(new FirestoreRepository.DataSyncCallback() {
            @Override
            public void onSuccess() {
                navigateToMainActivity();
            }

            @Override
            public void onError(String error) {
                Log.d("LoginActivity", "No se pudieron restaurar datos desde Firestore, usando datos locales: " + error);
                navigateToMainActivity();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //UsuarioActual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserDataAndNavigate();
        }
    }
}
