package com.example.expoporgra2prompt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etRegisterUser, etRegisterPassword;
    private Button btnRegister;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterUser = findViewById(R.id.etRegisterUser);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        dbHelper = new DBHelper(this);

        etRegisterUser.requestFocus();
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btnRegister.setOnClickListener(v -> {
            String user = etRegisterUser.getText().toString().trim();
            String pass = etRegisterPassword.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.registrarUsuario(user, pass)) {
                Toast.makeText(RegisterActivity.this, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
