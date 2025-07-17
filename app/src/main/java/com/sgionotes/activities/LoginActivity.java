package com.sgionotes.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.sgionotes.R;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    TextView registerRedirect;
    EditText txtEmail;
    EditText txtPassword;
    LinearLayout loginMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        String email = "pepito@gmail.com";
        String password = "123456";

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginMain = findViewById(R.id.loginMain);

        btnLogin.setOnClickListener(btn -> {

            if (txtEmail.getText().toString().isEmpty() ||
                    txtPassword.getText().toString().isEmpty()) {
                Snackbar.make(loginMain, "Completa los campos para acceder", Snackbar.LENGTH_LONG).show();
            }
            else if (txtEmail.getText().toString().equals(email) &&
                    txtPassword.getText().toString().equals(password)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                Snackbar.make(loginMain, "Usuario y/o contraseña inválidos", Snackbar.LENGTH_SHORT).show();
            }
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
}