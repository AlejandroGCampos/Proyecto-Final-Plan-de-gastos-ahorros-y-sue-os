package com.example.expoporgra2prompt;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;

public class GastosActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ListView lvGastos;
    private EditText etMonto, etDescripcion, etCategoria;
    private String currentUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gastos);
        dbHelper = new DBHelper(this);
        lvGastos = findViewById(R.id.lvGastos);
        etMonto = findViewById(R.id.etMonto);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCategoria = findViewById(R.id.etCategoria);
        Button btnGuardarGasto = findViewById(R.id.btnGuardarGasto);

        if (getIntent() != null && getIntent().hasExtra("usuario")) {
            currentUser = getIntent().getStringExtra("usuario");
        }

        cargarListaGastos();
        btnGuardarGasto.setOnClickListener(v -> guardarGasto());
    }

    private void cargarListaGastos() {
        Cursor cursor;
        if (currentUser != null && !currentUser.isEmpty()) {
            cursor = dbHelper.obtenerGastos(currentUser);
        } else {
            cursor = dbHelper.obtenerGastos("");
        }
        ArrayList<String> lista = new ArrayList<>();
        while (cursor.moveToNext()) {
            String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
            double monto = cursor.getDouble(cursor.getColumnIndexOrThrow("monto"));
            String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
            String item = String.format(Locale.getDefault(), "%s - $%.2f (%s)", descripcion, monto, categoria);
            lista.add(item);
        }
        cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        lvGastos.setAdapter(adapter);
    }

    private void guardarGasto() {
        String montoStr = etMonto.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        if (montoStr.isEmpty() || descripcion.isEmpty() || categoria.isEmpty()) {
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
        // Obtener la fecha actual en formato yyyy-MM-dd
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean exito = dbHelper.insertarGasto(currentUser, monto, descripcion, categoria, fecha);
        if (exito) {
            Toast.makeText(this, "Gasto guardado", Toast.LENGTH_SHORT).show();
            etMonto.setText("");
            etDescripcion.setText("");
            etCategoria.setText("");
            cargarListaGastos();
            // Enviar interacción al webhook de n8n
            N8nWebhook.enviarInteraccion(currentUser, "gasto", "agregar", monto, fecha, categoria);
        } else {
            Toast.makeText(this, "Error al guardar gasto", Toast.LENGTH_SHORT).show();
        }
    }
}
