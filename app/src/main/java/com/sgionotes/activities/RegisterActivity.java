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
import com.sgionotes.models.UserProfile;
import com.sgionotes.utils.UserProfileManager;
import java.util.regex.Pattern;
public class RegisterActivity extends AppCompatActivity {
    private TextView txtLoginRedirect;
    private EditText txtNombres;
    private EditText txtApellidos;
    private EditText txtCorreo;
    private EditText txtPassword;
    private LinearLayout registerMain;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        //InicializarFirebase
        mAuth = FirebaseAuth.getInstance();
        txtNombres = findViewById(R.id.txtNombres);
        txtApellidos = findViewById(R.id.txtApellidos);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtPassword = findViewById(R.id.txtPassword);
        txtLoginRedirect = findViewById(R.id.loginRedirect);
        registerMain = findViewById(R.id.registerMain);
        btnRegister = findViewById(R.id.btnRegister);
        txtLoginRedirect.setOnClickListener(btn -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("app_startup", false); // Indicar que NO es startup de la app
            startActivity(intent);
            finish();
        });
        btnRegister.setOnClickListener(btn -> {
            if (validacionRegister()) {
                btnRegister.setEnabled(false);
                btnRegister.setText("Creando cuenta...");
                String email = txtCorreo.getText().toString().trim();
                String password = txtPassword.getText().toString();
                registerWithFirebase(email, password);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void registerWithFirebase(String email, String password) {
        mAuth.signOut();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.getEmail() != null) {
                            Log.d("RegisterActivity", "Usuario registrado: " + user.getEmail() + " UID: " + user.getUid());
                            saveUserProfile();
                            Snackbar.make(registerMain, "Cuenta creada exitosamente", Snackbar.LENGTH_SHORT).show();
                            loadUserDataAndNavigate();
                        } else {
                            //Error
                            Snackbar.make(registerMain, "Error en el registro", Snackbar.LENGTH_SHORT).show();
                            btnRegister.setEnabled(true);
                            btnRegister.setText("Crear Cuenta");
                        }
                    } else {
                        //Error
                        String errorMessage = "Error al crear la cuenta";
                        if (task.getException() != null) {
                            String exception = task.getException().getMessage();
                            if (exception != null && exception.contains("email address is already in use")) {
                                errorMessage = "Este correo ya está registrado";
                            } else if (exception != null && exception.contains("weak password")) {
                                errorMessage = "La contraseña es muy débil";
                            }
                        }
                        Snackbar.make(registerMain, errorMessage, Snackbar.LENGTH_LONG).show();
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Crear Cuenta");
                    }
                });
    }
    private void loadUserDataAndNavigate() {
        android.os.Handler timeoutHandler = new android.os.Handler();
        final boolean[] navigationCompleted = {false};
        timeoutHandler.postDelayed(() -> {
            if (!navigationCompleted[0]) {
                navigationCompleted[0] = true;
                navigateToMainActivity();
            }
        }, 1500);
        com.sgionotes.models.GenerarData generarData = com.sgionotes.models.GenerarData.getInstancia();
        generarData.initializeWithContext(this);
        generarData.createDefaultDataIfEmptyWithCallback(new com.sgionotes.models.GenerarData.DataInitializationCallback() {
            @Override
            public void onInitializationComplete() {
                if (!navigationCompleted[0]) {
                    navigationCompleted[0] = true;
                    timeoutHandler.removeCallbacksAndMessages(null);
                    navigateToMainActivity();
                }
            }
            @Override
            public void onInitializationError(String error) {
                if (!navigationCompleted[0]) {
                    navigationCompleted[0] = true;
                    timeoutHandler.removeCallbacksAndMessages(null);
                    Log.w("RegisterActivity", "Error de inicialización: " + error);
                    navigateToMainActivity();
                }
            }
        });
    }
    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, com.sgionotes.activities.MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void saveUserProfile() {
        UserProfileManager profileManager = new UserProfileManager(this);

        String nombres = txtNombres.getText().toString().trim();
        String apellidos = txtApellidos.getText().toString().trim();
        String email = txtCorreo.getText().toString().trim();
        UserProfile profile = new UserProfile(nombres, apellidos, email, R.drawable.ic_person_24);
        profileManager.saveUserProfile(profile);
    }
    private boolean validacionRegister() {
        String nombres = txtNombres.getText().toString().trim();
        String apellidos = txtApellidos.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String password = txtPassword.getText().toString();
        if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Snackbar.make(registerMain, "Todos los campos son obligatorios", Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (nombres.length() > 80 || apellidos.length() > 80) {
            Snackbar.make(registerMain, "Nombres y apellidos deben tener máximo 80 caracteres", Snackbar.LENGTH_LONG).show();
            return false;
        }
        Pattern patternCorreo = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
        if (!patternCorreo.matcher(correo).matches()) {
            Snackbar.make(registerMain, "Correo no válido", Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (password.length() < 8 || password.length() > 50) {
            Snackbar.make(registerMain, "La contraseña debe tener entre 8 y 50 caracteres", Snackbar.LENGTH_LONG).show();
            return false;
        }
        Pattern patternPassword = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$");
        if (!patternPassword.matcher(password).matches()) {
            Snackbar.make(registerMain, "La contraseña debe tener mayúsculas, minúsculas, número y carácter especial", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}