package com.example.hospital;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hospital.ui.patient.PacienteActivity;
import com.example.hospital.ui.medico.MedicoActivity;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvMensaje;

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

        initViews();
        setupClickListeners();
        mostrarMensajeBienvenida();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        tvMensaje = findViewById(R.id.tvMensaje);
    }

    private void setupClickListeners() {
        // Botón Pacientes
        findViewById(R.id.btnPacientes).setOnClickListener(v -> {
            mostrarLoading(true);
            startActivity(new Intent(this, PacienteActivity.class));
            mostrarLoading(false);
        });

        // Botón Médicos
        findViewById(R.id.btnMedicos).setOnClickListener(v -> {
            mostrarLoading(true);
            startActivity(new Intent(this, MedicoActivity.class));
            mostrarLoading(false);
        });

        // Botón Citas
        findViewById(R.id.btnCitas).setOnClickListener(v -> {
            mostrarMensaje("Citas: Próximamente... 🏗️");
            Toast.makeText(this, "Módulo de Citas en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Botón Tratamientos
        findViewById(R.id.btnTratamientos).setOnClickListener(v -> {
            mostrarMensaje("Tratamientos: Próximamente... 🏗️");
            Toast.makeText(this, "Módulo de Tratamientos en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Botón Reportes
        findViewById(R.id.btnReportes).setOnClickListener(v -> {
            mostrarMensaje("Reportes: Próximamente... 🏗️");
            Toast.makeText(this, "Módulo de Reportes en desarrollo", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarMensajeBienvenida() {
        mostrarMensaje("🏥 Sistema de Hospital - Módulos: Pacientes ✅ Médicos ✅");
    }

    private void mostrarLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void mostrarMensaje(String mensaje) {
        if (tvMensaje != null) {
            tvMensaje.setText(mensaje);
            tvMensaje.setVisibility(View.VISIBLE);
        }
    }
}
