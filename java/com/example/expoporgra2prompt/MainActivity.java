package com.example.expoporgra2prompt;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private TextView tvBalance, tvConsejo;
    private int consejoIndex = 0;
    private String[] consejosIA;
    private String currentUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);

        if (getIntent() != null && getIntent().hasExtra("usuario")) {
            currentUser = getIntent().getStringExtra("usuario");
        }

        tvBalance = findViewById(R.id.tvBalance);
        tvConsejo = findViewById(R.id.tvConsejo);
        Button btnGastos = findViewById(R.id.btnGastos);
        Button btnAhorros = findViewById(R.id.btnAhorros);
        Button btnSuenos = findViewById(R.id.btnSuenos);
        Button btnReporte = findViewById(R.id.btnReporte);
        Button btnConsejoIA = findViewById(R.id.btnConsejoIA);
        Button btnVerMasConsejos = findViewById(R.id.btnVerMasConsejos);



        btnVerMasConsejos.setEnabled(true);
        btnVerMasConsejos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatFinanceActivity.class);
            intent.putExtra("usuario", currentUser);
            startActivity(intent);
        });

        btnGastos.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, GastosActivity.class);
            i.putExtra("usuario", currentUser);
            startActivity(i);
        });
        btnAhorros.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AhorrosActivity.class);
            i.putExtra("usuario", currentUser);
            startActivity(i);
        });
        btnSuenos.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SuenosActivity.class);
            i.putExtra("usuario", currentUser);
            startActivity(i);
        });
        btnReporte.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ReporteActivity.class);
            i.putExtra("usuario", currentUser);
            startActivity(i);
        });

        btnConsejoIA.setOnClickListener(v -> obtenerConsejoFinanciero());
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBalanceYConsejo();
    }

    private void actualizarBalanceYConsejo() {
        double balance;
        String consejo;
        if (currentUser != null && !currentUser.isEmpty()) {
            balance = dbHelper.obtenerBalanceGeneral(currentUser);
            consejo = dbHelper.advertirGastoCategoriaUsuario(currentUser, "Comida", 500);
        } else {
            balance = dbHelper.obtenerBalanceGeneral("");
            consejo = dbHelper.advertirGastoCategoria("Comida", 500);
        }
        String balanceText = String.format(Locale.getDefault(), "Balance total: $%.2f pesos", balance);
        tvBalance.setText(balanceText);
        tvConsejo.setText(getString(R.string.consejo_format, consejo));
    }

    private String obtenerApiKey() {
        // API key hardcoded directly as requested
        return "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";
    }

    private void obtenerConsejoFinanciero() {
        String apiKey = obtenerApiKey();
        String resumenDatos = obtenerResumenFinanciero();
        if (resumenDatos.trim().isEmpty() || resumenDatos.contains("no hay datos")) {
            runOnUiThread(() -> tvConsejo.setText(getString(R.string.no_data_message)));
            return;
        }
        if (apiKey.isEmpty()) {
            String consejoLocal = "Analiza tu presupuesto y prioriza ahorro.\n(Consejo local porque no hay API key)";
            consejosIA = generarConsejosDinamicos(resumenDatos, consejoLocal);
            consejoIndex = 0;
            runOnUiThread(() -> tvConsejo.setText(getString(R.string.consejo_format, consejosIA[consejoIndex])));
            return;
        }
        new Thread(() -> {
            try {
                String consejo = obtenerConsejoDesdeOpenAI(apiKey, resumenDatos);
                consejosIA = generarConsejosDinamicos(resumenDatos, consejo);
                consejoIndex = 0;
                runOnUiThread(() -> tvConsejo.setText(getString(R.string.consejo_format, consejosIA[consejoIndex])));
            } catch (Exception e) {
                Log.e("MainActivity", "Error al obtener consejo de OpenAI", e);
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_consejo_ia), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String[] generarConsejosDinamicos(String datos, String consejoPrincipal) {
        boolean gastoComidaAlto = datos.toLowerCase().contains("comida");
        String consejoComida = gastoComidaAlto ? "Reduce comidas fuera de casa y cocina más en casa." : "Mantén control sobre gastos en comida.";
        return new String[] {
                consejoPrincipal,
                consejoComida,
                "Considera aumentar tu ahorro mensual.",
                "Evalúa tus metas y ajusta tu presupuesto.",
                "Evita gastos innecesarios en entretenimiento si quieres ahorrar más."
        };
    }

    private String obtenerResumenFinanciero() {
        boolean hayDatos = false;
        StringBuilder resumen = new StringBuilder();
        double totalGastos = dbHelper.getTotalGastos(currentUser);
        if (totalGastos > 0) {
            resumen.append("Gastos: $").append(totalGastos);
            hayDatos = true;
        }
        double totalAhorros = dbHelper.getTotalAhorros(currentUser);
        if (totalAhorros > 0) {
            if (hayDatos) resumen.append(", ");
            resumen.append("Ahorros: $").append(totalAhorros);
            hayDatos = true;
        }
        Cursor metasCursor = dbHelper.obtenerMetas(currentUser);
        if (metasCursor != null && metasCursor.moveToFirst()) {
            if (hayDatos) resumen.append(", ");
            resumen.append("Metas: ");
            do {
                String nombre = metasCursor.getString(metasCursor.getColumnIndexOrThrow("nombre"));
                double objetivo = metasCursor.getDouble(metasCursor.getColumnIndexOrThrow("monto_objetivo"));
                double acumulado = metasCursor.getDouble(metasCursor.getColumnIndexOrThrow("monto_acumulado"));
                resumen.append(nombre).append(" ($").append(acumulado).append("/$").append(objetivo).append(") ");
            } while (metasCursor.moveToNext());
            metasCursor.close();
            hayDatos = true;
        } else if (metasCursor != null) {
            metasCursor.close();
        }
        if (!hayDatos) return "no hay datos";
        return resumen.toString();
    }

    // ✅ Conexión buena a OpenAI
    private String obtenerConsejoDesdeOpenAI(String apiKey, String promptUsuario) throws Exception {
        String endpoint = "https://api.openai.com/v1/chat/completions";
        java.net.URL url = new java.net.URL(endpoint);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        String jsonInputString = "{\"model\": \"gpt-4\",\"messages\": [{\"role\": \"user\", \"content\": \"" + promptUsuario + "\"}]}";
        try (java.io.OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = conn.getResponseCode();
        java.io.InputStream is = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        if (responseCode >= 200 && responseCode < 300) {
            org.json.JSONObject json = new org.json.JSONObject(response);
            org.json.JSONArray choices = json.getJSONArray("choices");
            if (choices.length() > 0) {
                org.json.JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                return message.getString("content").trim();
            } else {
                return "No se obtuvo respuesta del modelo de lenguaje";
            }
        } else {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Error al obtener consejo IA\\nHTTP code: ").append(responseCode);
            errorMsg.append("\\nRespuesta: ").append(response);
            try {
                org.json.JSONObject errorJson = new org.json.JSONObject(response);
                if (errorJson.has("error")) {
                    org.json.JSONObject err = errorJson.getJSONObject("error");
                    String msg = err.optString("message", "");
                    errorMsg.append("\\nMensaje API: ").append(msg);
                }
            } catch (Exception ex) {
                Log.e("MainActivity", "Error parsing error response", ex);
            }
            throw new Exception(errorMsg.toString());
        }
    }
}
