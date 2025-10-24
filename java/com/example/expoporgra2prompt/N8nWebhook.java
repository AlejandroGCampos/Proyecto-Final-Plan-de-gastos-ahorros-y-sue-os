package com.example.expoporgra2prompt;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class N8nWebhook {

    private static final String N8N_WEBHOOK_URL = "https://primary-production-59f2d.up.railway.app/webhook/ec3a5c58-58b9-49cb-8442-13e6a4c69362";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void enviarInteraccion(String usuario, String tipo, String accion, double monto, String fecha, String categoria) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("usuario", usuario);
                json.put("tipo", tipo);
                json.put("accion", accion);
                json.put("monto", monto);
                json.put("fecha", fecha);
                json.put("categoria", categoria);

                String jsonString = json.toString();
                Log.d("N8N_PAYLOAD", "JSON creado: " + jsonString);

                // --- LA LÍNEA FINAL Y CORRECTA ---
                // El orden correcto es: TIPO, y luego CONTENIDO.
                RequestBody body = RequestBody.create(JSON, jsonString);

                Request request = new Request.Builder()
                        .url(N8N_WEBHOOK_URL)
                        .post(body)
                        .build();

                getUnsafeOkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("N8N_ERROR", "Fallo catastrófico al enviar la petición: ", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.e("N8N_ERROR", "El servidor respondió con un error: " + response.code() + " Body: " + response.body().string());
                        } else {
                            Log.i("N8N_SUCCESS", "Petición enviada y aceptada por el servidor.");
                            response.body().close();
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e("N8N_JSON_ERROR", "Error al crear el objeto JSON", e);
            }
        }).start();
    }

    // Cliente OkHttp que ignora errores de certificado SSL (solo para depuración).
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}