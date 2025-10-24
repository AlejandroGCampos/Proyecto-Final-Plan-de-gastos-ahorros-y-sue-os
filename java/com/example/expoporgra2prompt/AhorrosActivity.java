package com.example.expoporgra2prompt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.view.View;
import android.widget.LinearLayout;
import com.example.expoporgra2prompt.N8nWebhook;

public class AhorrosActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    // btn will be local
    private String currentUser = "";
    private LinearLayout layoutMenuAhorro;
    private LinearLayout layoutAhorroGeneral;
    private LinearLayout layoutAhorroSueno;
    private Button btnMenuAhorroGeneral, btnMenuAhorroSueno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ahorros);
        dbHelper = new DBHelper(this);
        TextView tvTotalAhorros = findViewById(R.id.tvTotalAhorros);
        EditText etMontoAhorro = findViewById(R.id.etMontoAhorro);
        EditText etDescripcionAhorro = findViewById(R.id.etDescripcionAhorro);
        Button btnGuardarAhorro = findViewById(R.id.btnGuardarAhorro);
        Spinner spinnerMetas = findViewById(R.id.spinnerMetas);
        Button btnGuardarAhorroMeta = findViewById(R.id.btnGuardarAhorroMeta);
        layoutMenuAhorro = findViewById(R.id.layoutMenuAhorro);
        layoutAhorroGeneral = findViewById(R.id.layoutAhorroGeneral);
        layoutAhorroSueno = findViewById(R.id.layoutAhorroSueno);
        btnMenuAhorroGeneral = findViewById(R.id.btnMenuAhorroGeneral);
        btnMenuAhorroSueno = findViewById(R.id.btnMenuAhorroSueno);
        EditText etMontoAhorroMeta = findViewById(R.id.etMontoAhorroMeta);

        if (getIntent() != null && getIntent().hasExtra("usuario")) {
            currentUser = getIntent().getStringExtra("usuario");
        }

        // Al inicio, solo el menú está visible
        layoutMenuAhorro.setVisibility(View.VISIBLE);
        layoutAhorroGeneral.setVisibility(View.GONE);
        layoutAhorroSueno.setVisibility(View.GONE);

        btnMenuAhorroGeneral.setOnClickListener(v -> {
            layoutMenuAhorro.setVisibility(View.GONE);
            layoutAhorroGeneral.setVisibility(View.VISIBLE);
            layoutAhorroSueno.setVisibility(View.GONE);
        });
        btnMenuAhorroSueno.setOnClickListener(v -> {
            layoutMenuAhorro.setVisibility(View.GONE);
            layoutAhorroGeneral.setVisibility(View.GONE);
            layoutAhorroSueno.setVisibility(View.VISIBLE);
        });

        mostrarTotalAhorros(tvTotalAhorros);
        cargarMetasEnSpinner(spinnerMetas);
        btnGuardarAhorro.setOnClickListener(v -> guardarAhorro(etMontoAhorro, etDescripcionAhorro, tvTotalAhorros));
        btnGuardarAhorroMeta.setOnClickListener(v -> guardarAhorroParaMeta(spinnerMetas, etMontoAhorroMeta, tvTotalAhorros));
    }

    private void mostrarTotalAhorros(TextView tvTotalAhorros) {
        Cursor cursor = null;
        double total = 0;
        try {
            if (currentUser != null && !currentUser.isEmpty()) {
                cursor = dbHelper.obtenerAhorros(currentUser);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        total += cursor.getDouble(cursor.getColumnIndexOrThrow("monto"));
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al obtener ahorros", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        tvTotalAhorros.setText(String.format(Locale.getDefault(), "Total Ahorros: $%.2f", total));
    }

    private void cargarMetasEnSpinner(Spinner spinnerMetas) {
        ArrayList<String> listaMetas = new ArrayList<>();
        Cursor cursor = dbHelper.obtenerMetas(currentUser);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String nombreMeta = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                listaMetas.add(nombreMeta);
            } while (cursor.moveToNext());
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaMetas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMetas.setAdapter(adapter);
    }

    private void guardarAhorro(EditText etMontoAhorro, EditText etDescripcionAhorro, TextView tvTotalAhorros) {
        String montoStr = etMontoAhorro.getText().toString().trim();
        String descripcion = etDescripcionAhorro.getText().toString().trim();

        if (montoStr.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Ningún campo debe quedar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (monto <= 0) {
            Toast.makeText(this, "El monto debe ser positivo", Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean exito = dbHelper.insertarAhorro(currentUser, monto, descripcion, fecha);
        if (exito) {
            Toast.makeText(this, "Ahorro guardado", Toast.LENGTH_SHORT).show();
            etMontoAhorro.setText("");
            etDescripcionAhorro.setText("");
            mostrarTotalAhorros(tvTotalAhorros);
            // Enviar interacción al webhook de n8n
            N8nWebhook.enviarInteraccion(currentUser, "ahorro", "agregar", monto, fecha, descripcion);
        } else {
            Toast.makeText(this, "Error al guardar ahorro", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarAhorroParaMeta(Spinner spinnerMetas, EditText etMontoAhorroMeta, TextView tvTotalAhorros) {
        String montoStr = etMontoAhorroMeta.getText().toString().trim();
        if (montoStr.isEmpty()) {
            Toast.makeText(this, "Debe ingresar un monto", Toast.LENGTH_SHORT).show();
            return;
        }
        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (monto <= 0) {
            Toast.makeText(this, "El monto debe ser positivo", Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = spinnerMetas.getSelectedItemPosition();
        if (pos < 0) {
            Toast.makeText(this, "Seleccione una meta válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el ID de la meta seleccionada
        Cursor cursor = dbHelper.obtenerMetas(currentUser);
        int idMeta = -1;
        if (cursor != null && cursor.moveToPosition(pos)) {
            idMeta = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        }

        if (idMeta == -1) {
            Toast.makeText(this, "Error al obtener la meta seleccionada", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exito = dbHelper.agregarAhorroAMeta(idMeta, monto);
        if (exito) {
            Toast.makeText(this, "Ahorro agregado a la meta", Toast.LENGTH_SHORT).show();
            etMontoAhorroMeta.setText("");
            mostrarTotalAhorros(tvTotalAhorros);
            // Enviar interacción al webhook de n8n
            String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String nombreMeta = spinnerMetas.getSelectedItem().toString();
            N8nWebhook.enviarInteraccion(currentUser, "ahorro", "agregar", monto, fecha, nombreMeta);
        } else {
            Toast.makeText(this, "Error al agregar ahorro a la meta", Toast.LENGTH_SHORT).show();
        }
    }
}
