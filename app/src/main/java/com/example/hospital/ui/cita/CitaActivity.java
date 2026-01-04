package com.example.hospital.ui.cita;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.MainActivity;
import com.example.hospital.R;
import com.example.hospital.data.models.Cita;
import com.example.hospital.data.models.EstadoCita;
import com.example.hospital.data.models.Medico;
import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.repository.MedicoRepository;
import com.example.hospital.data.repository.PacienteRepository;
import com.example.hospital.viewmodel.CitaViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class CitaActivity extends AppCompatActivity implements CitaAdapter.OnCitaClickListener {

    // UI Components
    private TextInputEditText etHora;
    private AutoCompleteTextView etPaciente;
    private AutoCompleteTextView etMedico;
    private Spinner spEstado;
    private Spinner spDia;
    private RecyclerView rvCitas;
    private ProgressBar progressBar;
    private TextView tvMensaje;

    // Botones de filtro
    private Button btnTodos, btnProgramadas, btnAtendidas, btnCanceladas, btnBuscar;

    // ViewModel y Adapter
    private CitaViewModel citaViewModel;
    private CitaAdapter citaAdapter;

    // Repositorios para autocomplete
    private MedicoRepository medicoRepository;
    private PacienteRepository pacienteRepository;
    
    // Arrays para autocomplete
    private ArrayAdapter<String> pacienteAdapter;
    private ArrayAdapter<String> medicoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita);

        initViews();
        setupViewModel();
        setupRepositories();
        setupRecyclerView();
        setupAutocomplete();
        setupSpinners();
        setupClickListeners();
    }

    private void initViews() {
        etHora = findViewById(R.id.etHora);
        etPaciente = findViewById(R.id.etPaciente);
        etMedico = findViewById(R.id.etMedico);
        spEstado = findViewById(R.id.spEstado);
        spDia = findViewById(R.id.spDia);
        rvCitas = findViewById(R.id.rvCitas);
        progressBar = findViewById(R.id.progressBar);

        // Botones de filtro
        btnTodos = findViewById(R.id.btnTodos);
        btnProgramadas = findViewById(R.id.btnProgramadas);
        btnAtendidas = findViewById(R.id.btnAtendidas);
        btnCanceladas = findViewById(R.id.btnCanceladas);
        btnBuscar = findViewById(R.id.btnBuscar);
    }

    private void setupViewModel() {
        citaViewModel = new ViewModelProvider(this).get(CitaViewModel.class);

        // Observar LiveData
        citaViewModel.getCitas().observe(this, this::actualizarListaCitas);
        citaViewModel.getMensaje().observe(this, this::mostrarMensaje);
        citaViewModel.getLoading().observe(this, this::mostrarLoading);
    }

    private void setupRepositories() {
        medicoRepository = new MedicoRepository(this);
        pacienteRepository = new PacienteRepository(this);
    }

    private void setupRecyclerView() {
        citaAdapter = new CitaAdapter();
        citaAdapter.setOnCitaClickListener(this);

        rvCitas.setLayoutManager(new LinearLayoutManager(this));
        rvCitas.setAdapter(citaAdapter);
    }

    private void setupAutocomplete() {
        // Configurar adapter para pacientes
        pacienteAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line);
        etPaciente.setAdapter(pacienteAdapter);
        
        // Configurar adapter para médicos
        medicoAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line);
        etMedico.setAdapter(medicoAdapter);
        
        // Cargar datos para autocomplete
        cargarAutocompleteData();
    }

    private void cargarAutocompleteData() {
        // Cargar pacientes
        List<Paciente> pacientes = pacienteRepository.getAllPacientes();
        pacienteAdapter.clear();
        for (Paciente paciente : pacientes) {
            pacienteAdapter.add(paciente.getCorreo());
        }
        pacienteAdapter.notifyDataSetChanged();

        // Cargar médicos
        List<Medico> medicos = medicoRepository.getAllMedicos();
        medicoAdapter.clear();
        for (Medico medico : medicos) {
            medicoAdapter.add(medico.getCorreo());
        }
        medicoAdapter.notifyDataSetChanged();
    }

    private void setupSpinners() {
        // Spinner de días
        String[] dias = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"};
        ArrayAdapter<String> diasAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, dias);
        diasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDia.setAdapter(diasAdapter);

        // Spinner de estados
        EstadoCita[] estados = EstadoCita.values();
        ArrayAdapter<EstadoCita> estadosAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, estados);
        estadosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEstado.setAdapter(estadosAdapter);
    }

    private void setupClickListeners() {
        // Botón guardar
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarCita());

        // Botón cancelar
        findViewById(R.id.btnCancelar).setOnClickListener(v -> limpiarFormulario());

        // Botones de filtro
        btnTodos.setOnClickListener(v -> {
            citaViewModel.cargarCitas();
            actualizarBotonActivo(btnTodos);
        });

        btnProgramadas.setOnClickListener(v -> {
            citaViewModel.cargarCitasProgramadas();
            actualizarBotonActivo(btnProgramadas);
        });

        btnAtendidas.setOnClickListener(v -> {
            citaViewModel.cargarCitasAtendidas();
            actualizarBotonActivo(btnAtendidas);
        });

        btnCanceladas.setOnClickListener(v -> {
            citaViewModel.cargarCitasCanceladas();
            actualizarBotonActivo(btnCanceladas);
        });

        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        // Establecer botón "Todos" como activo por defecto
        actualizarBotonActivo(btnTodos);
    }

    private void mostrarDialogoBusqueda() {
        View view = getLayoutInflater().inflate(R.layout.dialog_busqueda_cita, null);

        Spinner spCampo = view.findViewById(R.id.spCampoBusqueda);
        AutoCompleteTextView etTermino = view.findViewById(R.id.etTerminoBusqueda);

        // Configurar spinner de campos
        String[] campos = {"Paciente", "Médico", "Día"};
        ArrayAdapter<String> camposAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, campos);
        camposAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCampo.setAdapter(camposAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Buscar Citas")
                .setView(view)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String termino = etTermino.getText().toString().trim();
                    String campo = (String) spCampo.getSelectedItem();

                    if (!TextUtils.isEmpty(termino)) {
                        switch (campo) {
                            case "Paciente":
                                citaViewModel.filtrarPorPaciente(termino);
                                break;
                            case "Médico":
                                citaViewModel.filtrarPorMedico(termino);
                                break;
                            case "Día":
                                citaViewModel.filtrarPorDia(termino.toUpperCase());
                                break;
                        }
                        actualizarBotonActivo(btnBuscar);
                    } else {
                        Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarBotonActivo(Button botonActivo) {
        // Resetear todos los botones a estilo outline
        btnTodos.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnProgramadas.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnAtendidas.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnCanceladas.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnBuscar.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));

        // Resaltar botón activo
        botonActivo.setBackgroundColor(getResources().getColor(R.color.primary, getTheme()));
    }

    private void guardarCita() {
        String horaStr = etHora.getText().toString().trim();
        String paciente = etPaciente.getText().toString().trim();
        String medico = etMedico.getText().toString().trim();
        String diaStr = (String) spDia.getSelectedItem();
        EstadoCita estado = (EstadoCita) spEstado.getSelectedItem();

        // Validaciones básicas
        if (TextUtils.isEmpty(horaStr)) {
            Toast.makeText(this, "Ingrese la hora", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(paciente)) {
            Toast.makeText(this, "Seleccione el paciente", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(medico)) {
            Toast.makeText(this, "Seleccione el médico", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de hora
        if (!horaStr.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            Toast.makeText(this, "Formato de hora inválido. Use HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        String diaIngles = convertirDiaEspanolIngles(diaStr);
        citaViewModel.guardarCita(horaStr, diaIngles, paciente, medico, estado, false);
    }

    private void actualizarListaCitas(List<Cita> citas) {
        citaAdapter.actualizarCitas(citas);
    }

    private void mostrarMensaje(String mensaje) {
        if (tvMensaje != null) {
            if (mensaje != null && !mensaje.isEmpty()) {
                tvMensaje.setVisibility(View.VISIBLE);
                tvMensaje.setText(mensaje);
            } else {
                tvMensaje.setVisibility(View.GONE);
            }
        }

        // Mostrar siempre en Toast para feedback inmediato
        if (mensaje != null && !mensaje.isEmpty()) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();

            // Si es un mensaje de éxito, limpiar formulario
            if (mensaje.contains("guardada exitosamente") || mensaje.contains("eliminada exitosamente")) {
                limpiarFormulario();
            }
        }
    }

    private void mostrarLoading(Boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void limpiarFormulario() {
        etHora.setText("");
        etPaciente.setText("");
        etMedico.setText("");
        spDia.setSelection(0);
        spEstado.setSelection(0); // PROGRAMADA
        etHora.requestFocus(); // Poner foco en el primer campo
    }

    private String convertirDiaEspanolIngles(String diaEspanol) {
        switch (diaEspanol) {
            case "LUNES":
                return "MONDAY";
            case "MARTES":
                return "TUESDAY";
            case "MIÉRCOLES":
                return "WEDNESDAY";
            case "JUEVES":
                return "THURSDAY";
            case "VIERNES":
                return "FRIDAY";
            case "SÁBADO":
                return "SATURDAY";
            case "DOMINGO":
                return "SUNDAY";
            default:
                return diaEspanol; // fallback
        }
    }

    private String convertirDiaInglesEspanol(String diaIngles) {
        switch (diaIngles) {
            case "MONDAY":
                return "LUNES";
            case "TUESDAY":
                return "MARTES";
            case "WEDNESDAY":
                return "MIÉRCOLES";
            case "THURSDAY":
                return "JUEVES";
            case "FRIDAY":
                return "VIERNES";
            case "SATURDAY":
                return "SÁBADO";
            case "SUNDAY":
                return "DOMINGO";
            default:
                return diaIngles; // fallback
        }
    }

    @Override
    public void onCitaClick(Cita cita) {
        // Cargar datos de la cita para edición
        etHora.setText(cita.getHora().toString());
        etPaciente.setText(cita.getPaciente());
        etMedico.setText(cita.getMedico());

        // Seleccionar día
        String diaEspanol = convertirDiaInglesEspanol(cita.getDia().name());
        String[] dias = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"};
        for (int i = 0; i < dias.length; i++) {
            if (dias[i].equals(diaEspanol)) {
                spDia.setSelection(i);
                break;
            }
        }

        // Seleccionar estado
        EstadoCita[] estados = EstadoCita.values();
        for (int i = 0; i < estados.length; i++) {
            if (estados[i] == cita.getEstadoCita()) {
                spEstado.setSelection(i);
                break;
            }
        }

        Toast.makeText(this, "Cita seleccionada: ID " + cita.getIdCita(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCitaDelete(Cita cita) {
        // Mostrar confirmación antes de eliminar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Cita")
                .setMessage("¿Está seguro que desea eliminar la cita ID " + cita.getIdCita() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    citaViewModel.eliminarCita(cita.getIdCita());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onCitaCancel(Cita cita) {
        citaViewModel.cancelarCita(cita.getIdCita());
    }

    @Override
    public void onCitaAttend(Cita cita) {
        citaViewModel.marcarComoAtendida(cita.getIdCita());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Volver al menú principal
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}