package com.example.expoporgra2prompt;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SuenosActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ListView lvMetas;
    private EditText etNombreMeta, etMontoMeta, etDescripcionMeta;
    private Button btnGuardarMeta;
    private String currentUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suenos);
        dbHelper = new DBHelper(this);
        lvMetas = findViewById(R.id.lvMetas);
        etNombreMeta = findViewById(R.id.etNombreMeta);
        etMontoMeta = findViewById(R.id.etMontoMeta);
        etDescripcionMeta = findViewById(R.id.etDescripcionMeta);
        btnGuardarMeta = findViewById(R.id.btnGuardarMeta);

        if (getIntent() != null && getIntent().hasExtra("usuario")) {
            currentUser = getIntent().getStringExtra("usuario");
        }

        cargarListaMetas();
        // Se establece el listener para el botón
        btnGuardarMeta.setOnClickListener(v -> guardarMeta());
    }

    private void cargarListaMetas() {
        Cursor cursor = dbHelper.obtenerMetas(currentUser);
        ArrayList<String> lista = new ArrayList<>();
        if (cursor == null) return; // Protección contra cursor nulo
        while (cursor.moveToNext()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            double objetivo = cursor.getDouble(cursor.getColumnIndexOrThrow("monto_objetivo"));
            double acumulado = cursor.getDouble(cursor.getColumnIndexOrThrow("monto_acumulado"));
            String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
            int progreso = objetivo > 0 ? (int)((acumulado / objetivo) * 100) : 0;
            String item = String.format(Locale.US, "%s: %s\nMeta: $%.2f | Acumulado: $%.2f | Progreso: %d%%",
                    nombre, descripcion, objetivo, acumulado, progreso);
            lista.add(item);
        }
        cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        lvMetas.setAdapter(adapter);
    }

    private void guardarMeta() {
        String nombre = etNombreMeta.getText().toString().trim();
        String montoStr = etMontoMeta.getText().toString().trim();
        String descripcion = etDescripcionMeta.getText().toString().trim();
        if (nombre.isEmpty() || montoStr.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Ningún campo debe quedar vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        double montoObjetivo;
        try {
            montoObjetivo = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido para el monto", Toast.LENGTH_SHORT).show();
            return;
        }
        if (montoObjetivo <= 0) {
            Toast.makeText(this, "El monto objetivo debe ser positivo", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean exito = dbHelper.insertarMeta(currentUser, nombre, montoObjetivo, descripcion);
        if (exito) {
            Toast.makeText(this, "Meta agregada correctamente", Toast.LENGTH_SHORT).show();
            
            // --- INICIO DE LA LÓGICA DE ENVÍO ---
            // 1. Obtener la fecha actual
            String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // 2. Llamar al método para enviar los datos al webhook
            // Parámetros: usuario, tipo, acción, monto, fecha, categoría
            N8nWebhook.enviarInteraccion(currentUser, "Sueño", "Agregar", montoObjetivo, fecha, nombre);
            // --- FIN DE LA LÓGICA DE ENVÍO ---

            // Limpiar los campos y recargar la lista
            etNombreMeta.setText("");
            etMontoMeta.setText("");
            etDescripcionMeta.setText("");
            cargarListaMetas();

        } else {
            Toast.makeText(this, "Error al agregar la meta", Toast.LENGTH_SHORT).show();
        }
    }
}