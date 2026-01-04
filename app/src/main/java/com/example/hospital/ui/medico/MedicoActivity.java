package com.example.hospital.ui.medico;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.hospital.data.models.HorarioAtencion;
import com.example.hospital.data.models.Medico;
import com.example.hospital.viewmodel.MedicoViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;

public class MedicoActivity extends AppCompatActivity implements MedicoAdapter.OnMedicoClickListener {

    // UI Components
    private TextInputEditText etNombre, etApellido, etCorreo, etCedula, etEspecialidad;
    private RadioGroup rgGenero, rgActivo;
    private RecyclerView rvMedicos;
    private ProgressBar progressBar;
    private TextView tvMensaje;

    // Botones de filtro
    private Button btnTodos, btnActivos, btnMasculino, btnFemenino, btnBuscar;

    // Botones de horario
    private Button btnConfigurarHorario;

    // ViewModel y Adapter
    private MedicoViewModel medicoViewModel;
    private MedicoAdapter medicoAdapter;

    // Horario de atención actual
    private HorarioAtencion horarioAtencionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etCorreo = findViewById(R.id.etCorreo);
        etCedula = findViewById(R.id.etCedula);
        etEspecialidad = findViewById(R.id.etEspecialidad);
        rgGenero = findViewById(R.id.rgGenero);
        rgActivo = findViewById(R.id.rgActivo);
        rvMedicos = findViewById(R.id.rvMedicos);
        progressBar = findViewById(R.id.progressBar);

        // Botones de filtro
        btnTodos = findViewById(R.id.btnTodos);
        btnActivos = findViewById(R.id.btnActivos);
        btnMasculino = findViewById(R.id.btnMasculino);
        btnFemenino = findViewById(R.id.btnFemenino);
        btnBuscar = findViewById(R.id.btnBuscar);

        // Botones de acción
        btnConfigurarHorario = findViewById(R.id.btnConfigurarHorario);
    }

    private void setupViewModel() {
        medicoViewModel = new ViewModelProvider(this).get(MedicoViewModel.class);

        // Observar LiveData
        medicoViewModel.getMedicos().observe(this, this::actualizarListaMedicos);
        medicoViewModel.getMensaje().observe(this, this::mostrarMensaje);
        medicoViewModel.getLoading().observe(this, this::mostrarLoading);
    }

    private void setupRecyclerView() {
        medicoAdapter = new MedicoAdapter();
        medicoAdapter.setOnMedicoClickListener(this);

        rvMedicos.setLayoutManager(new LinearLayoutManager(this));
        rvMedicos.setAdapter(medicoAdapter);
    }

    private void setupClickListeners() {
        // Botón guardar
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarMedico());

        // Botón cancelar
        findViewById(R.id.btnCancelar).setOnClickListener(v -> limpiarFormulario());

        // Botones de filtro
        btnTodos.setOnClickListener(v -> {
            medicoViewModel.cargarMedicos();
            actualizarBotonActivo(btnTodos);
        });

        btnActivos.setOnClickListener(v -> {
            medicoViewModel.cargarMedicosActivos();
            actualizarBotonActivo(btnActivos);
        });

        btnMasculino.setOnClickListener(v -> {
            medicoViewModel.filtrarPorGenero("Masculino");
            actualizarBotonActivo(btnMasculino);
        });

        btnFemenino.setOnClickListener(v -> {
            medicoViewModel.filtrarPorGenero("Femenino");
            actualizarBotonActivo(btnFemenino);
        });

        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        // Botón configurar horario
        btnConfigurarHorario.setOnClickListener(v -> mostrarDialogoHorario());

        // Establecer botón "Todos" como activo por defecto
        actualizarBotonActivo(btnTodos);
    }

    private void mostrarDialogoBusqueda() {
        View view = getLayoutInflater().inflate(R.layout.dialog_busqueda_medico, null);

        RadioGroup rgCampo = view.findViewById(R.id.rgCampoBusqueda);
        TextInputEditText etTermino = view.findViewById(R.id.etTerminoBusqueda);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Buscar Médicos")
                .setView(view)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String termino = etTermino.getText().toString().trim();
                    int selectedId = rgCampo.getCheckedRadioButtonId();

                    if (!TextUtils.isEmpty(termino)) {
                        if (selectedId == R.id.rbBuscarCorreo) {
                            medicoViewModel.buscarPorCorreo(termino);
                        } else if (selectedId == R.id.rbBuscarEspecialidad) {
                            medicoViewModel.filtrarPorEspecialidad(termino);
                        }
                        actualizarBotonActivo(btnBuscar);
                    } else {
                        Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoHorario() {
        View view = getLayoutInflater().inflate(R.layout.dialog_horario, null);

        TextInputEditText etHoraInicio = view.findViewById(R.id.etHoraInicio);
        TextInputEditText etHoraFin = view.findViewById(R.id.etHoraFin);
        TextInputEditText etDuracionCita = view.findViewById(R.id.etDuracionCita);
        
        // Checkboxes para días
        CheckBox cbLunes = view.findViewById(R.id.cbLunes);
        CheckBox cbMartes = view.findViewById(R.id.cbMartes);
        CheckBox cbMiercoles = view.findViewById(R.id.cbMiercoles);
        CheckBox cbJueves = view.findViewById(R.id.cbJueves);
        CheckBox cbViernes = view.findViewById(R.id.cbViernes);
        CheckBox cbSabado = view.findViewById(R.id.cbSabado);
        CheckBox cbDomingo = view.findViewById(R.id.cbDomingo);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Configurar Horario de Atención")
                .setView(view)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String horaInicioStr = etHoraInicio.getText().toString().trim();
                    String horaFinStr = etHoraFin.getText().toString().trim();
                    String duracionStr = etDuracionCita.getText().toString().trim();

                    if (TextUtils.isEmpty(horaInicioStr) || TextUtils.isEmpty(horaFinStr) || TextUtils.isEmpty(duracionStr)) {
                        Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        LocalTime horaInicio = LocalTime.parse(horaInicioStr);
                        LocalTime horaFin = LocalTime.parse(horaFinStr);
                        int duracion = Integer.parseInt(duracionStr);

                        if (!horaFin.isAfter(horaInicio)) {
                            Toast.makeText(this, "La hora fin debe ser posterior a la hora inicio", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Construir conjunto de días seleccionados
                        EnumSet<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
                        if (cbLunes.isChecked()) dias.add(DayOfWeek.MONDAY);
                        if (cbMartes.isChecked()) dias.add(DayOfWeek.TUESDAY);
                        if (cbMiercoles.isChecked()) dias.add(DayOfWeek.WEDNESDAY);
                        if (cbJueves.isChecked()) dias.add(DayOfWeek.THURSDAY);
                        if (cbViernes.isChecked()) dias.add(DayOfWeek.FRIDAY);
                        if (cbSabado.isChecked()) dias.add(DayOfWeek.SATURDAY);
                        if (cbDomingo.isChecked()) dias.add(DayOfWeek.SUNDAY);

                        if (dias.isEmpty()) {
                            Toast.makeText(this, "Seleccione al menos un día de atención", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Crear horario y establecer duración
                        horarioAtencionActual = new HorarioAtencion(horaInicio, horaFin, dias);
                        
                        Toast.makeText(this, "Horario configurado correctamente", Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "La duración debe ser un número válido", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Formato de hora inválido. Use HH:mm", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarBotonActivo(Button botonActivo) {
        // Resetear todos los botones a estilo outline
        btnTodos.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnActivos.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnMasculino.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnFemenino.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnBuscar.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));

        // Resaltar botón activo
        botonActivo.setBackgroundColor(getResources().getColor(R.color.primary, getTheme()));
    }

    private void guardarMedico() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String cedula = etCedula.getText().toString().trim();
        String especialidad = etEspecialidad.getText().toString().trim();

        // Obtener género seleccionado
        String genero = getGeneroSeleccionado();
        boolean activo = getActivoSeleccionado();

        // Validaciones básicas
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) ||
                TextUtils.isEmpty(correo) || TextUtils.isEmpty(cedula) ||
                TextUtils.isEmpty(especialidad)) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!correo.contains("@")) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (horarioAtencionActual == null) {
            Toast.makeText(this, "Configure el horario de atención", Toast.LENGTH_SHORT).show();
            return;
        }

        medicoViewModel.guardarMedico(nombre, apellido, correo, cedula, genero, especialidad, horarioAtencionActual, activo);
    }

    private String getGeneroSeleccionado() {
        int selectedId = rgGenero.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMasculino) {
            return "Masculino";
        } else {
            return "Femenino";
        }
    }

    private boolean getActivoSeleccionado() {
        int selectedId = rgActivo.getCheckedRadioButtonId();
        return selectedId == R.id.rbActivo;
    }

    private void actualizarListaMedicos(List<Medico> medicos) {
        medicoAdapter.actualizarMedicos(medicos);
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
            if (mensaje.contains("guardado exitosamente") || mensaje.contains("eliminado exitosamente")) {
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
        etNombre.setText("");
        etApellido.setText("");
        etCorreo.setText("");
        etCedula.setText("");
        etEspecialidad.setText("");
        rgGenero.check(R.id.rbMasculino); // Volver al default
        rgActivo.check(R.id.rbActivo); // Volver al default
        horarioAtencionActual = null;
        etNombre.requestFocus(); // Poner foco en el primer campo
    }

    @Override
    public void onMedicoClick(Medico medico) {
        // Cargar datos del médico para edición
        etNombre.setText(medico.getNombre());
        etApellido.setText(medico.getApellido());
        etCorreo.setText(medico.getCorreo());
        etCedula.setText(medico.getCedulaString());
        etEspecialidad.setText(medico.getEspecialidad());

        // Seleccionar género
        if ("Masculino".equalsIgnoreCase(medico.getGenero())) {
            rgGenero.check(R.id.rbMasculino);
        } else {
            rgGenero.check(R.id.rbFemenino);
        }

        // Seleccionar estado activo
        if (medico.isActivo()) {
            rgActivo.check(R.id.rbActivo);
        } else {
            rgActivo.check(R.id.rbInactivo);
        }

        // Cargar horario
        horarioAtencionActual = medico.getHorarioAtencion();

        Toast.makeText(this, "Médico seleccionado: " + medico.getNombre(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMedicoDelete(Medico medico) {
        // Mostrar confirmación antes de eliminar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Médico")
                .setMessage("¿Está seguro que desea eliminar al Dr. " + medico.getNombre() + " " + medico.getApellido()
                        + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    medicoViewModel.eliminarMedico(medico.getId());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onMedicoToggleStatus(Medico medico) {
        medicoViewModel.activarDesactivarMedico(medico.getId());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Volver al menú principal
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}