package com.example.hospital.ui.tratamiento;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.MainActivity;
import com.example.hospital.R;
import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.models.TratamientoPaciente;
import com.example.hospital.data.repository.PacienteRepository;
import com.example.hospital.viewmodel.TratamientoPacienteViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class TratamientoActivity extends AppCompatActivity implements TratamientoAdapter.OnTratamientoClickListener {

    // UI Components
    private AutoCompleteTextView etCorreoPaciente;
    private TextInputEditText etNombreTratamiento;
    private TextInputEditText etCostoUnidad, etVecesDia, etDias;
    private TextInputEditText etCostoCirugia;
    private TextInputEditText etCostoSesion, etNumeroSesiones;
    private RadioGroup rgTipoTratamiento;
    private RecyclerView rvTratamientos;
    private ProgressBar progressBar;
    private TextView tvMensaje, tvPacienteActual;

    // Layouts dinámicos
    private LinearLayout llCamposMedicacion, llCamposCirugia, llCamposTerapia;

    // Botones de filtro
    private Button btnTodos, btnActivos, btnCompletados, btnBuscar;
    private Button btnFiltroActivo;

    // ViewModel y Adapter
    private TratamientoPacienteViewModel tratamientoViewModel;
    private TratamientoAdapter tratamientoAdapter;
    
    // Repositorio y adapter para autocomplete
    private PacienteRepository pacienteRepository;
    private ArrayAdapter<String> pacienteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tratamiento);

        initViews();
        setupViewModel();
        setupRepositories();
        setupRecyclerView();
        setupAutocomplete();
        setupClickListeners();
        setupTipoTratamientoListener();
        
        // Cargar todos los tratamientos al iniciar
        tratamientoViewModel.cargarTodos();
    }

    private void initViews() {
        etCorreoPaciente = findViewById(R.id.etCorreoPaciente);
        etNombreTratamiento = findViewById(R.id.etNombreTratamiento);
        
        // Campos para medicación
        etCostoUnidad = findViewById(R.id.etCostoUnidad);
        etVecesDia = findViewById(R.id.etVecesDia);
        etDias = findViewById(R.id.etDias);
        
        // Campos para cirugía
        etCostoCirugia = findViewById(R.id.etCostoCirugia);
        
        // Campos para terapia
        etCostoSesion = findViewById(R.id.etCostoSesion);
        etNumeroSesiones = findViewById(R.id.etNumeroSesiones);
        
        rgTipoTratamiento = findViewById(R.id.rgTipoTratamiento);
        rvTratamientos = findViewById(R.id.rvTratamientos);
        progressBar = findViewById(R.id.progressBar);
        tvMensaje = findViewById(R.id.tvMensaje);
        tvPacienteActual = findViewById(R.id.tvPacienteActual);

        // Layouts dinámicos
        llCamposMedicacion = findViewById(R.id.llCamposMedicacion);
        llCamposCirugia = findViewById(R.id.llCamposCirugia);
        llCamposTerapia = findViewById(R.id.llCamposTerapia);

        // Botones de filtro
        btnTodos = findViewById(R.id.btnTodos);
        btnActivos = findViewById(R.id.btnActivos);
        btnCompletados = findViewById(R.id.btnCompletados);
        btnBuscar = findViewById(R.id.btnBuscar);
    }

    private void setupViewModel() {
        tratamientoViewModel = new ViewModelProvider(this).get(TratamientoPacienteViewModel.class);

        // Observar LiveData
        tratamientoViewModel.getTratamientos().observe(this, this::actualizarListaTratamientos);
        tratamientoViewModel.getMensaje().observe(this, this::mostrarMensaje);
        tratamientoViewModel.getLoading().observe(this, this::mostrarLoading);
        tratamientoViewModel.getPacienteActual().observe(this, this::actualizarPacienteActual);
    }

    private void setupRepositories() {
        pacienteRepository = new PacienteRepository(this);
    }

    private void setupAutocomplete() {
        // Configurar adapter para pacientes
        pacienteAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line);
        etCorreoPaciente.setAdapter(pacienteAdapter);
        
        // Cargar datos para autocomplete
        cargarAutocompleteData();
        
        // Configurar listener para cuando se seleccione un paciente
        etCorreoPaciente.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = (String) parent.getItemAtPosition(position);
            // Extraer el correo del formato "Nombre Apellido (correo@ejemplo.com)"
            String correoExtraido = extraerCorreo(seleccionado);
            
            // Al seleccionar un paciente, limpiar el formulario para facilitar nueva entrada
            limpiarFormularioExceptoCorreo();
            etCorreoPaciente.setText(correoExtraido);
        });
    }

    private void cargarAutocompleteData() {
        // Cargar pacientes
        List<Paciente> pacientes = pacienteRepository.getAllPacientes();
        pacienteAdapter.clear();
        for (Paciente paciente : pacientes) {
            // Solo agregar "Nombre Apellido (correo)" para mostrar más información
            pacienteAdapter.add(paciente.getNombre() + " " + paciente.getApellido() + " (" + paciente.getCorreo() + ")");
        }
        pacienteAdapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        tratamientoAdapter = new TratamientoAdapter();
        tratamientoAdapter.setOnTratamientoClickListener(this);

        rvTratamientos.setLayoutManager(new LinearLayoutManager(this));
        rvTratamientos.setAdapter(tratamientoAdapter);
    }

    private void setupClickListeners() {
        // Botón guardar
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarTratamiento());

        // Botón cancelar
        findViewById(R.id.btnCancelar).setOnClickListener(v -> limpiarFormulario());

        // Botones de filtro
        btnTodos.setOnClickListener(v -> {
            tratamientoViewModel.cargarTodos();
            actualizarBotonActivo(btnTodos);
        });

        btnActivos.setOnClickListener(v -> {
            tratamientoViewModel.cargarPorEstado("ACTIVO");
            actualizarBotonActivo(btnActivos);
        });

        btnCompletados.setOnClickListener(v -> {
            tratamientoViewModel.cargarPorEstado("COMPLETADO");
            actualizarBotonActivo(btnCompletados);
        });

        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        // Establecer botón "Todos" como activo por defecto
        actualizarBotonActivo(btnTodos);
    }

    private void setupTipoTratamientoListener() {
        rgTipoTratamiento.setOnCheckedChangeListener((group, checkedId) -> {
            // Ocultar todos los campos específicos
            llCamposMedicacion.setVisibility(View.GONE);
            llCamposCirugia.setVisibility(View.GONE);
            llCamposTerapia.setVisibility(View.GONE);

            // Mostrar campos según el tipo seleccionado
            if (checkedId == R.id.rbMedicacion) {
                llCamposMedicacion.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbCirugia) {
                llCamposCirugia.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbTerapia) {
                llCamposTerapia.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarDialogoBusqueda() {
        View view = getLayoutInflater().inflate(R.layout.dialog_busqueda, null);

        RadioGroup rgCampo = view.findViewById(R.id.rgCampoBusqueda);
        TextInputEditText etTermino = view.findViewById(R.id.etTerminoBusqueda);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Buscar Tratamientos")
                .setView(view)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String termino = etTermino.getText().toString().trim();

                    if (!TextUtils.isEmpty(termino)) {
                        tratamientoViewModel.buscarTratamientos(termino);
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
        btnActivos.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnCompletados.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnBuscar.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));

        // Resaltar botón activo
        botonActivo.setBackgroundColor(getResources().getColor(R.color.primary, getTheme()));
    }

    private void guardarTratamiento() {
        String correoPaciente = etCorreoPaciente.getText().toString().trim();
        String nombre = etNombreTratamiento.getText().toString().trim();
        
        // Validaciones básicas
        if (TextUtils.isEmpty(correoPaciente)) {
            Toast.makeText(this, "Ingrese el correo del paciente", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "Ingrese el nombre del tratamiento", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que se haya seleccionado un tipo de tratamiento
        int selectedId = rgTipoTratamiento.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Seleccione el tipo de tratamiento", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener valores según el tipo de tratamiento
        String tipoTratamiento = "";
        String valor1 = "", valor2 = "", valor3 = "";
        boolean camposValidos = true;

        if (selectedId == R.id.rbMedicacion) {
            tipoTratamiento = "MEDICACIÓN";
            valor1 = etCostoUnidad.getText().toString().trim(); // costo unidad
            valor2 = etVecesDia.getText().toString().trim();    // veces al día
            valor3 = etDias.getText().toString().trim();         // días
            
            if (TextUtils.isEmpty(valor1) || TextUtils.isEmpty(valor2) || TextUtils.isEmpty(valor3)) {
                Toast.makeText(this, "Complete todos los campos de la medicación", Toast.LENGTH_SHORT).show();
                camposValidos = false;
            }
        } else if (selectedId == R.id.rbCirugia) {
            tipoTratamiento = "CIRUGÍA";
            valor1 = etCostoCirugia.getText().toString().trim(); // costo total
            
            if (TextUtils.isEmpty(valor1)) {
                Toast.makeText(this, "Ingrese el costo de la cirugía", Toast.LENGTH_SHORT).show();
                camposValidos = false;
            }
        } else if (selectedId == R.id.rbTerapia) {
            tipoTratamiento = "TERAPIA";
            valor1 = etCostoSesion.getText().toString().trim();    // costo sesión
            valor2 = etNumeroSesiones.getText().toString().trim(); // número de sesiones
            
            if (TextUtils.isEmpty(valor1) || TextUtils.isEmpty(valor2)) {
                Toast.makeText(this, "Complete todos los campos de la terapia", Toast.LENGTH_SHORT).show();
                camposValidos = false;
            }
        }

        if (camposValidos) {
            tratamientoViewModel.guardarTratamientoPaciente(correoPaciente, tipoTratamiento, 
                                                           nombre, valor1, valor2, valor3);
        }
    }

    private void actualizarListaTratamientos(List<TratamientoPaciente> tratamientos) {
        tratamientoAdapter.actualizarTratamientosPaciente(tratamientos);
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
            if (mensaje.contains("asignado exitosamente")) {
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
        etCorreoPaciente.setText("");
        limpiarFormularioExceptoCorreo();
    }

    private String extraerCorreo(String texto) {
        // Busca el patrón (correo@dominio.com) y extrae solo el correo
        int inicio = texto.lastIndexOf('(');
        int fin = texto.lastIndexOf(')');
        
        if (inicio != -1 && fin != -1 && fin > inicio) {
            return texto.substring(inicio + 1, fin);
        }
        
        // Si no encuentra el formato, devuelve el texto tal cual (por si escribió el correo directamente)
        return texto;
    }

    private void limpiarFormularioExceptoCorreo() {
        etNombreTratamiento.setText("");
        
        // Limpiar campos de medicación
        etCostoUnidad.setText("");
        etVecesDia.setText("");
        etDias.setText("");
        
        // Limpiar campos de cirugía
        etCostoCirugia.setText("");
        
        // Limpiar campos de terapia
        etCostoSesion.setText("");
        etNumeroSesiones.setText("");
        
        // Resetear tipo de tratamiento
        rgTipoTratamiento.clearCheck();
        
        // Ocultar todos los campos específicos
        llCamposMedicacion.setVisibility(View.GONE);
        llCamposCirugia.setVisibility(View.GONE);
        llCamposTerapia.setVisibility(View.GONE);
        
        etNombreTratamiento.requestFocus();
    }

    private void actualizarPacienteActual(Paciente paciente) {
        if (paciente != null) {
            tvPacienteActual.setText("Paciente actual: " + paciente.getNombre() + " " + 
                                   paciente.getApellido() + " (" + paciente.getCorreo() + ")");
            // Precargar correo del paciente para facilitar más tratamientos
            etCorreoPaciente.setText(paciente.getCorreo());
        } else {
            tvPacienteActual.setText("Paciente actual: No seleccionado");
        }
    }

    @Override
    public void onTratamientoClick(TratamientoPaciente tratamiento) {
        // Cargar correo del paciente para facilitar más tratamientos
        etCorreoPaciente.setText(tratamiento.getPaciente().getCorreo());
        
        // Mostrar detalles del tratamiento
        StringBuilder detalles = new StringBuilder();
        detalles.append("📋 TRATAMIENTO SELECCIONADO\n\n");
        detalles.append("👤 Paciente: ").append(tratamiento.getPaciente().getNombre())
                .append(" ").append(tratamiento.getPaciente().getApellido()).append("\n");
        detalles.append("📧 Correo: ").append(tratamiento.getPaciente().getCorreo()).append("\n");
        detalles.append("💊 Tratamiento: ").append(tratamiento.getTratamiento().getNombre()).append("\n");
        detalles.append("🏷️ Tipo: ").append(tratamiento.getTratamiento().getTipo()).append("\n");
        detalles.append("📅 Fecha asignación: ").append(tratamiento.getFechaFormateada()).append("\n");
        detalles.append("📊 Estado: ").append(tratamiento.getEstado()).append("\n");
        detalles.append("💰 Costo total: $").append(String.format("%.2f", tratamiento.getCostoTotal())).append("\n\n");
        
        detalles.append("📝 Detalles del tratamiento:\n");
        detalles.append(tratamiento.getDetallesTratamiento());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Detalles del Tratamiento")
                .setMessage(detalles.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("Cambiar Estado", (dialog, which) -> mostrarDialogoCambiarEstado(tratamiento))
                .show();
    }

    @Override
    public void onTratamientoDelete(TratamientoPaciente tratamiento) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Tratamiento")
                .setMessage("¿Está seguro que desea eliminar este tratamiento?\n\n" +
                           "Paciente: " + tratamiento.getPaciente().getNombre() + " " + tratamiento.getPaciente().getApellido() + "\n" +
                           "Tratamiento: " + tratamiento.getTratamiento().getNombre())
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    tratamientoViewModel.eliminarTratamientoPaciente(tratamiento);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onTratamientoShowCost(TratamientoPaciente tratamiento) {
        onTratamientoClick(tratamiento); // Reutilizar el mismo método para mostrar detalles
    }

    private void mostrarDialogoCambiarEstado(TratamientoPaciente tratamiento) {
        String[] estados = {"ACTIVO", "COMPLETADO", "CANCELADO"};
        int currentIndex = 0;
        
        // Encontrar el índice del estado actual
        for (int i = 0; i < estados.length; i++) {
            if (estados[i].equals(tratamiento.getEstado())) {
                currentIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cambiar Estado")
                .setSingleChoiceItems(estados, currentIndex, (dialog, which) -> {
                    String nuevoEstado = estados[which];
                    tratamientoViewModel.cambiarEstadoTratamiento(tratamiento, nuevoEstado);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}