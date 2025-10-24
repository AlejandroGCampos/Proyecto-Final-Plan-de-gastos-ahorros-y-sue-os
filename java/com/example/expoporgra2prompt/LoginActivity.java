package com.example.expoporgra2prompt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etLoginUser, etLoginPassword;
    private Button btnLogin, btnGoRegister;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginUser = findViewById(R.id.etLoginUser);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        dbHelper = new DBHelper(this);

        etLoginUser.requestFocus();
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btnLogin.setOnClickListener(v -> {
            String user = etLoginUser.getText().toString().trim();
            String pass = etLoginPassword.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!dbHelper.existeUsuario(user)) {
                Toast.makeText(LoginActivity.this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.validarUsuario(user, pass)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("usuario", user);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
