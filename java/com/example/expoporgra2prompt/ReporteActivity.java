package com.example.expoporgra2prompt;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.Locale;

public class ReporteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        TextView tvTotalGastos = findViewById(R.id.tvTotalGastos);
        TextView tvTotalAhorros = findViewById(R.id.tvTotalAhorros);
        TextView tvBalance = findViewById(R.id.tvBalance);
        PieChart pieChart = findViewById(R.id.pieChart);

        DBHelper dbHelper = new DBHelper(this);

        // Obtener usuario desde Intent (si existe)
        String usuario = null;
        if (getIntent() != null && getIntent().hasExtra("usuario")) {
            usuario = getIntent().getStringExtra("usuario");
        }

        double totalGastos, totalAhorros, balance;
        if (usuario != null && !usuario.isEmpty()) {
            totalGastos = dbHelper.getTotalGastos(usuario);
            totalAhorros = dbHelper.getTotalAhorros(usuario);
            balance = dbHelper.obtenerBalanceGeneral(usuario);
        } else {
            totalGastos = dbHelper.getTotalGastos();
            totalAhorros = dbHelper.getTotalAhorros();
            balance = totalAhorros - totalGastos;
        }

        // Asegurarse de que el balance y las cantidades se formateen correctamente antes de mostrarse
        tvTotalGastos.setText(String.format(Locale.getDefault(), "%.2f", totalGastos));
        tvTotalAhorros.setText(String.format(Locale.getDefault(), "%.2f", totalAhorros));
        // Mostrar el balance con texto "Balance: " seguido de la cantidad
        tvBalance.setText(String.format(Locale.getDefault(), "Balance: %.2f", balance));

        // Mostrar gráfica
        PieEntry gastosEntry = new PieEntry((float) totalGastos, "Gastos");
        PieEntry ahorrosEntry = new PieEntry((float) totalAhorros, "Ahorros");
        PieEntry balanceEntry = new PieEntry((float) Math.max(0, balance), "Balance");
        PieDataSet dataSet = new PieDataSet(java.util.Arrays.asList(gastosEntry, ahorrosEntry, balanceEntry), "Reporte Financiero");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setCenterText("Reporte");
        if (pieChart.getDescription() != null) pieChart.getDescription().setEnabled(false);
        pieChart.animateY(800);
    }
}
