package com.example.hospital.ui.reporte;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hospital.MainActivity;
import com.example.hospital.R;
import com.example.hospital.data.models.Cita;
import com.example.hospital.data.models.TratamientoPaciente;
import com.example.hospital.viewmodel.ReporteViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Map;

public class ReporteActivity extends AppCompatActivity {

    // UI Components - General
    private TextView tvResumen;
    private TextView tvMensaje;
    private ProgressBar progressBar;
    private Button btnLimpiarFiltros;
    private Button btnRecargar;

    // UI Components - Reporte 1: Citas por Especialidad
    private AutoCompleteTextView etEspecialidad;
    private Button btnFiltrarEspecialidad;
    private TextView tvCitasPorEspecialidad;
    private ArrayAdapter<String> especialidadesAdapter;

    // UI Components - Reporte 2: Ingresos por Tratamientos
    private Spinner spTipoTratamiento;
    private Button btnFiltrarTipo;
    private TextView tvIngresosPorTratamiento;
    private ArrayAdapter<String> tiposTratamientoAdapter;

    // UI Components - Reporte 3: Historial por Paciente
    private AutoCompleteTextView etPaciente;
    private Button btnFiltrarPaciente;
    private TextView tvHistorialPaciente;
    private ArrayAdapter<String> pacientesAdapter;

    // ViewModel
    private ReporteViewModel reporteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        try {
            initViews();
            setupViewModel();
            setupAdapters();
            setupClickListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Error al inicializar módulo de reportes: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initViews() {
        // Componentes generales
        tvResumen = findViewById(R.id.tvResumen);
        tvMensaje = findViewById(R.id.tvMensaje);
        progressBar = findViewById(R.id.progressBar);
        btnLimpiarFiltros = findViewById(R.id.btnLimpiarFiltros);
        btnRecargar = findViewById(R.id.btnRecargar);

        // Reporte 1: Citas por Especialidad
        etEspecialidad = findViewById(R.id.etEspecialidad);
        btnFiltrarEspecialidad = findViewById(R.id.btnFiltrarEspecialidad);
        tvCitasPorEspecialidad = findViewById(R.id.tvCitasPorEspecialidad);

        // Reporte 2: Ingresos por Tratamientos
        spTipoTratamiento = findViewById(R.id.spTipoTratamiento);
        btnFiltrarTipo = findViewById(R.id.btnFiltrarTipo);
        tvIngresosPorTratamiento = findViewById(R.id.tvIngresosPorTratamiento);

        // Reporte 3: Historial por Paciente
        etPaciente = findViewById(R.id.etPaciente);
        btnFiltrarPaciente = findViewById(R.id.btnFiltrarPaciente);
        tvHistorialPaciente = findViewById(R.id.tvHistorialPaciente);
    }

    private void setupViewModel() {
        reporteViewModel = new ViewModelProvider(this).get(ReporteViewModel.class);

        // Observar LiveData
        reporteViewModel.getEstadisticasGenerales().observe(this, this::actualizarResumen);
        reporteViewModel.getCitasPorEspecialidad().observe(this, this::actualizarCitasPorEspecialidad);
        reporteViewModel.getCitasFiltradasPorEspecialidad().observe(this, this::mostrarDialogoCitasFiltradas);
        reporteViewModel.getIngresosPorTratamientos().observe(this, this::actualizarIngresosPorTratamientos);
        reporteViewModel.getIngresosFiltradosPorTipo().observe(this, this::mostrarDialogoIngresosFiltrados);
        reporteViewModel.getHistorialPorPaciente().observe(this, this::actualizarHistorialPorPaciente);
        reporteViewModel.getHistorialFiltradoPorPaciente().observe(this, this::mostrarDialogoHistorialFiltrado);

        reporteViewModel.getEspecialidadesDisponibles().observe(this, this::cargarEspecialidades);
        reporteViewModel.getTiposTratamientoDisponibles().observe(this, this::cargarTiposTratamiento);
        reporteViewModel.getPacientesConTratamientos().observe(this, this::cargarPacientes);

        reporteViewModel.getMensaje().observe(this, this::mostrarMensaje);
        reporteViewModel.getLoading().observe(this, this::mostrarLoading);
    }

    private void setupAdapters() {
        especialidadesAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line);
        etEspecialidad.setAdapter(especialidadesAdapter);

        tiposTratamientoAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item);
        tiposTratamientoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoTratamiento.setAdapter(tiposTratamientoAdapter);

        pacientesAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line);
        etPaciente.setAdapter(pacientesAdapter);
    }

    private void setupClickListeners() {
        // Reporte 1: Citas por Especialidad
        btnFiltrarEspecialidad.setOnClickListener(v -> {
            String especialidad = etEspecialidad.getText().toString().trim();
            if (!TextUtils.isEmpty(especialidad)) {
                reporteViewModel.filtrarCitasPorEspecialidad(especialidad);
            } else {
                Toast.makeText(this, "Ingrese una especialidad", Toast.LENGTH_SHORT).show();
            }
        });

        etEspecialidad.setOnItemClickListener((parent, view, position, id) -> {
            String especialidad = (String) parent.getItemAtPosition(position);
            reporteViewModel.filtrarCitasPorEspecialidad(especialidad);
        });

        // Reporte 2: Ingresos por Tratamientos
        btnFiltrarTipo.setOnClickListener(v -> {
            String tipo = (String) spTipoTratamiento.getSelectedItem();
            if (tipo != null && !tipo.equals("Todos")) {
                reporteViewModel.filtrarIngresosPorTipoTratamiento(tipo);
            } else {
                reporteViewModel.cargarIngresosTotalesPorTratamientos();
            }
        });

        spTipoTratamiento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = (String) parent.getItemAtPosition(position);
                if (tipo != null && !tipo.equals("Todos")) {
                    reporteViewModel.filtrarIngresosPorTipoTratamiento(tipo);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Reporte 3: Historial por Paciente
        btnFiltrarPaciente.setOnClickListener(v -> {
            String paciente = etPaciente.getText().toString().trim();
            if (!TextUtils.isEmpty(paciente)) {
                // Extraer correo del formato "Nombre (correo@ejemplo.com)"
                String correo = extraerCorreoDeString(paciente);
                if (correo != null) {
                    reporteViewModel.filtrarHistorialPorPaciente(correo);
                } else {
                    Toast.makeText(this, "Formato de paciente inválido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Ingrese un paciente", Toast.LENGTH_SHORT).show();
            }
        });

        etPaciente.setOnItemClickListener((parent, view, position, id) -> {
            String paciente = (String) parent.getItemAtPosition(position);
            String correo = extraerCorreoDeString(paciente);
            if (correo != null) {
                reporteViewModel.filtrarHistorialPorPaciente(correo);
            }
        });

        // Botones generales
        btnLimpiarFiltros.setOnClickListener(v -> {
            reporteViewModel.limpiarFiltros();
            etEspecialidad.setText("");
            etPaciente.setText("");
            spTipoTratamiento.setSelection(0);
        });

        btnRecargar.setOnClickListener(v -> {
            reporteViewModel.recargarTodosLosReportes();
        });
    }

    // Métodos para actualizar UI con los datos de los reportes
    private void actualizarResumen(Map<String, Object> estadisticas) {
        tvResumen.setText(reporteViewModel.generarResumenReportes());
    }

    private void actualizarCitasPorEspecialidad(Map<String, Integer> datos) {
        StringBuilder sb = new StringBuilder();
        if (datos.isEmpty()) {
            sb.append("No hay citas atendidas registradas");
        } else {
            sb.append("CITAS ATENDIDAS POR ESPECIALIDAD:\n\n");
            for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                sb.append("• ").append(entry.getKey())
                  .append(": ").append(entry.getValue()).append(" citas\n");
            }
            
            int total = datos.values().stream().mapToInt(Integer::intValue).sum();
            sb.append("\nTOTAL: ").append(total).append(" citas atendidas");
        }
        tvCitasPorEspecialidad.setText(sb.toString());
    }

    private void actualizarIngresosPorTratamientos(Map<String, Double> datos) {
        StringBuilder sb = new StringBuilder();
        if (datos.isEmpty()) {
            sb.append("No hay ingresos registrados");
        } else {
            sb.append("INGRESOS TOTALES POR TIPO:\n\n");
            for (Map.Entry<String, Double> entry : datos.entrySet()) {
                sb.append("• ").append(entry.getKey())
                  .append(": $").append(String.format("%.2f", entry.getValue())).append("\n");
            }
            
            double total = datos.values().stream().mapToDouble(Double::doubleValue).sum();
            sb.append("\nTOTAL INGRESOS: $").append(String.format("%.2f", total));
        }
        tvIngresosPorTratamiento.setText(sb.toString());
    }

    private void actualizarHistorialPorPaciente(Map<String, List<TratamientoPaciente>> datos) {
        StringBuilder sb = new StringBuilder();
        if (datos.isEmpty()) {
            sb.append("No hay tratamientos asignados");
        } else {
            sb.append("HISTORIAL DE TRATAMIENTOS:\n\n");
            for (Map.Entry<String, List<TratamientoPaciente>> entry : datos.entrySet()) {
                sb.append("👤 ").append(entry.getKey()).append(":\n");
                sb.append("   ").append(entry.getValue().size()).append(" tratamientos\n\n");
            }
            
            int totalTratamientos = datos.values().stream().mapToInt(List::size).sum();
            sb.append("TOTAL: ").append(totalTratamientos).append(" tratamientos asignados");
        }
        tvHistorialPaciente.setText(sb.toString());
    }

    // Métodos para cargar datos en los adapters
    private void cargarEspecialidades(List<String> especialidades) {
        especialidadesAdapter.clear();
        especialidadesAdapter.addAll(especialidades);
        especialidadesAdapter.notifyDataSetChanged();
    }

    private void cargarTiposTratamiento(List<String> tipos) {
        tiposTratamientoAdapter.clear();
        tiposTratamientoAdapter.add("Todos");
        tiposTratamientoAdapter.addAll(tipos);
        tiposTratamientoAdapter.notifyDataSetChanged();
    }

    private void cargarPacientes(List<String> pacientes) {
        pacientesAdapter.clear();
        pacientesAdapter.addAll(pacientes);
        pacientesAdapter.notifyDataSetChanged();
    }

    // Métodos para mostrar diálogos de detalles
    private void mostrarDialogoCitasFiltradas(List<Cita> citas) {
        if (citas == null || citas.isEmpty()) {
            Toast.makeText(this, "No hay citas para mostrar", Toast.LENGTH_SHORT).show();
            return;
        }

        String especialidad = etEspecialidad.getText().toString().trim();
        
        View view = getLayoutInflater().inflate(R.layout.dialog_detalle_citas, null);
        TextView tvEspecialidadTitulo = view.findViewById(R.id.tvEspecialidadTitulo);
        TextView tvCitasDetalle = view.findViewById(R.id.tvCitasDetalle);
        TextView tvTotalCitas = view.findViewById(R.id.tvTotalCitas);

        tvEspecialidadTitulo.setText("Especialidad: " + especialidad);
        
        StringBuilder sb = new StringBuilder();
        for (Cita cita : citas) {
            sb.append("ID: ").append(cita.getIdCita()).append("\n");
            sb.append("  Paciente: ").append(cita.getPaciente()).append("\n");
            sb.append("  Médico: ").append(cita.getMedico()).append("\n");
            sb.append("  Día: ").append(cita.getDia()).append("\n");
            sb.append("  Hora: ").append(cita.getHora()).append("\n");
            sb.append("  ━━━━━━━━━━━━━━━\n");
        }
        
        tvCitasDetalle.setText(sb.toString());
        tvTotalCitas.setText("Total: " + citas.size() + " citas");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Detalle de Citas")
                .setView(view)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarDialogoIngresosFiltrados(Double ingresos) {
        if (ingresos == null) {
            return;
        }

        String tipo = (String) spTipoTratamiento.getSelectedItem();
        
        View view = getLayoutInflater().inflate(R.layout.dialog_detalle_ingresos, null);
        TextView tvTipoTratamientoTitulo = view.findViewById(R.id.tvTipoTratamientoTitulo);
        TextView tvIngresosDetalle = view.findViewById(R.id.tvIngresosDetalle);
        TextView tvTotalIngresos = view.findViewById(R.id.tvTotalIngresos);

        tvTipoTratamientoTitulo.setText("Tipo: " + tipo);
        tvIngresosDetalle.setText("Ingresos generados por tratamientos de tipo: " + tipo);
        tvTotalIngresos.setText("$" + String.format("%.2f", ingresos));

        new MaterialAlertDialogBuilder(this)
                .setTitle("Detalle de Ingresos")
                .setView(view)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarDialogoHistorialFiltrado(List<TratamientoPaciente> tratamientos) {
        if (tratamientos == null || tratamientos.isEmpty()) {
            Toast.makeText(this, "No hay tratamientos para mostrar", Toast.LENGTH_SHORT).show();
            return;
        }

        String pacienteStr = etPaciente.getText().toString().trim();
        
        View view = getLayoutInflater().inflate(R.layout.dialog_historial_paciente, null);
        TextView tvPacienteTitulo = view.findViewById(R.id.tvPacienteTitulo);
        TextView tvHistorialDetalle = view.findViewById(R.id.tvHistorialDetalle);
        TextView tvTotalTratamientos = view.findViewById(R.id.tvTotalTratamientos);

        tvPacienteTitulo.setText("Paciente: " + pacienteStr);
        
        StringBuilder sb = new StringBuilder();
        double costoTotal = 0;
        for (TratamientoPaciente tp : tratamientos) {
            sb.append("ID: ").append(tp.getId()).append("\n");
            sb.append("  Tratamiento: ").append(tp.getTratamiento().getNombre()).append("\n");
            sb.append("  Tipo: ").append(tp.getTratamiento().getTipo()).append("\n");
            sb.append("  Costo: $").append(String.format("%.2f", tp.getCostoTotal())).append("\n");
            sb.append("  Fecha: ").append(tp.getFechaFormateada()).append("\n");
            sb.append("  Estado: ").append(tp.getEstado()).append("\n");
            sb.append("  ━━━━━━━━━━━━━━━\n");
            costoTotal += tp.getCostoTotal();
        }
        
        tvHistorialDetalle.setText(sb.toString());
        tvTotalTratamientos.setText("Total: " + tratamientos.size() + " tratamientos (Costo: $" + 
                                    String.format("%.2f", costoTotal) + ")");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Historial de Tratamientos")
                .setView(view)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // Métodos utilitarios
    private String extraerCorreoDeString(String texto) {
        if (texto == null) return null;
        
        int inicio = texto.lastIndexOf('(');
        int fin = texto.lastIndexOf(')');
        
        if (inicio != -1 && fin != -1 && fin > inicio) {
            return texto.substring(inicio + 1, fin);
        }
        
        return null;
    }

    private void mostrarMensaje(String mensaje) {
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            tvMensaje.setText(mensaje);
            tvMensaje.setVisibility(View.VISIBLE);
            
            // Mostrar también en Toast para feedback inmediato
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            
            // Ocultar mensaje después de 3 segundos
            tvMensaje.postDelayed(() -> tvMensaje.setVisibility(View.GONE), 3000);
        } else {
            tvMensaje.setVisibility(View.GONE);
        }
    }

    private void mostrarLoading(Boolean isLoading) {
        if (isLoading != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Volver al menú principal
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}