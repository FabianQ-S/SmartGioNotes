package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.sgionotes.R;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextView txtLoginRedirect;
    private EditText txtNombres;
    private EditText txtApellidos;
    private EditText txtCorreo;
    private EditText txtPassword;
    private LinearLayout registerMain;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);


        txtNombres = findViewById(R.id.txtNombres);
        txtApellidos = findViewById(R.id.txtApellidos);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtPassword = findViewById(R.id.txtPassword);
        txtLoginRedirect = findViewById(R.id.loginRedirect);
        registerMain = findViewById(R.id.registerMain);
        btnRegister = findViewById(R.id.btnRegister);

        txtLoginRedirect.setOnClickListener(btn -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnRegister.setOnClickListener(btn -> {
            if (validacionRegister()) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("registro_exitoso", true);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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