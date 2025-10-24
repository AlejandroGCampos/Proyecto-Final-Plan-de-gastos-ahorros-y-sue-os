package com.example.expoporgra2prompt;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ChatFinanceActivity extends AppCompatActivity {
    private RecyclerView rvMensajes;
    private EditText etMensaje;
    private ChatAdapter chatAdapter;
    private List<MensajeChat> mensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_finance);

        rvMensajes = findViewById(R.id.rvMensajes);
        etMensaje = findViewById(R.id.etMensaje);
        MaterialButton btnEnviar = findViewById(R.id.btnEnviar);

        mensajes = new ArrayList<>();
        chatAdapter = new ChatAdapter(mensajes);
        rvMensajes.setLayoutManager(new LinearLayoutManager(this));
        rvMensajes.setAdapter(chatAdapter);

        btnEnviar.setOnClickListener(v -> {
            String pregunta = etMensaje.getText().toString().trim();
            if (pregunta.isEmpty()) return;
            mensajes.add(new MensajeChat(pregunta, true));
            chatAdapter.notifyItemInserted(mensajes.size() - 1);
            rvMensajes.scrollToPosition(mensajes.size() - 1);
            etMensaje.setText("");
            obtenerRespuestaFinanceBot(pregunta);
        });
    }

    private String obtenerApiKey() {
        // API key hardcoded directly as requested
        return "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";
    }

    private void obtenerRespuestaFinanceBot(String pregunta) {
        mensajes.add(new MensajeChat("[FinanceBot] Procesando tu consulta...", false));
        chatAdapter.notifyItemInserted(mensajes.size() - 1);
        rvMensajes.scrollToPosition(mensajes.size() - 1);

        new Thread(() -> {
            try {
                String apiKey = obtenerApiKey();
                String endpoint = "https://api.openai.com/v1/chat/completions";

                // Build payload with org.json to avoid escaping issues
                org.json.JSONObject payloadObj = new org.json.JSONObject();
                payloadObj.put("model", "gpt-3.5-turbo");
                org.json.JSONArray messages = new org.json.JSONArray();
                org.json.JSONObject sys = new org.json.JSONObject();
                sys.put("role", "system");
                sys.put("content", "Eres un asistente útil y puedes responder cualquier tipo de consulta.");
                messages.put(sys);
                org.json.JSONObject user = new org.json.JSONObject();
                user.put("role", "user");
                user.put("content", pregunta);
                messages.put(user);
                payloadObj.put("messages", messages);
                String payload = payloadObj.toString();
                Log.d("ChatFinanceActivity", "Payload: " + payload);

                java.net.URL url = new java.net.URL(endpoint);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (java.io.OutputStream os = connection.getOutputStream()) {
                    os.write(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                Log.d("ChatFinanceActivity", "Response code: " + responseCode);

                java.io.InputStream is = (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                Log.d("ChatFinanceActivity", "API Response: " + response);

                if (responseCode >= 200 && responseCode < 300) {
                    // Success: parse the assistant's reply
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
                    org.json.JSONArray choices = jsonResponse.optJSONArray("choices");
                    if (choices != null && choices.length() > 0) {
                        org.json.JSONObject firstChoice = choices.getJSONObject(0);
                        org.json.JSONObject messageObj = firstChoice.optJSONObject("message");
                        String respuesta = messageObj != null ? messageObj.optString("content", "") : "";
                        Log.d("ChatFinanceActivity", "Parsed Response: " + respuesta);
                        runOnUiThread(() -> {
                            mensajes.add(new MensajeChat(respuesta, false));
                            chatAdapter.notifyItemInserted(mensajes.size() - 1);
                            rvMensajes.scrollToPosition(mensajes.size() - 1);
                        });
                    } else {
                        // No choices returned
                        runOnUiThread(() -> {
                            mensajes.add(new MensajeChat("Error: Respuesta sin opciones del servidor.", false));
                            chatAdapter.notifyItemInserted(mensajes.size() - 1);
                            rvMensajes.scrollToPosition(mensajes.size() - 1);
                        });
                    }
                } else {
                    // Error response from API
                    org.json.JSONObject errorObj = new org.json.JSONObject(response).optJSONObject("error");
                    String errorMsg = errorObj != null ? errorObj.optString("message", "Error desconocido") : "Respuesta inesperada del servidor";
                    runOnUiThread(() -> {
                        mensajes.add(new MensajeChat("Error de OpenAI: " + errorMsg, false));
                        chatAdapter.notifyItemInserted(mensajes.size() - 1);
                        rvMensajes.scrollToPosition(mensajes.size() - 1);
                    });
                }
            } catch (Exception e) {
                Log.e("ChatFinanceActivity", "Error during API call", e);
                runOnUiThread(() -> {
                    mensajes.add(new MensajeChat("Hubo un error al procesar tu consulta. Por favor, intenta nuevamente.", false));
                    chatAdapter.notifyItemInserted(mensajes.size() - 1);
                    rvMensajes.scrollToPosition(mensajes.size() - 1);
                });
            }
        }).start();
    }
}
