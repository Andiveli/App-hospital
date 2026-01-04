package com.example.hospital.ui.patient;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.R;
import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.models.TipoSeguro;
import com.example.hospital.viewmodel.PacienteViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class PacienteActivity extends AppCompatActivity implements PacienteAdapter.OnPacienteClickListener {

    // UI Components
    private TextInputEditText etNombre, etApellido, etCorreo, etCedula;
    private RadioGroup rgTipoSeguro;
    private RecyclerView rvPacientes;
    private ProgressBar progressBar;
    private TextView tvMensaje;

    // Botones de filtro
    private Button btnTodos, btnIESS, btnPrivado, btnBuscar;
    private Button btnFiltroActivo;

    // ViewModel y Adapter
    private PacienteViewModel pacienteViewModel;
    private PacienteAdapter pacienteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente);

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
        rgTipoSeguro = findViewById(R.id.rgTipoSeguro);
        rvPacientes = findViewById(R.id.rvPacientes);
        progressBar = findViewById(R.id.progressBar);

        // Botones de filtro
        btnTodos = findViewById(R.id.btnTodos);
        btnIESS = findViewById(R.id.btnIESS);
        btnPrivado = findViewById(R.id.btnPrivado);
        btnBuscar = findViewById(R.id.btnBuscar);
    }

    private void setupViewModel() {
        pacienteViewModel = new ViewModelProvider(this).get(PacienteViewModel.class);

        // Observar LiveData
        pacienteViewModel.getPacientes().observe(this, this::actualizarListaPacientes);
        pacienteViewModel.getMensaje().observe(this, this::mostrarMensaje);
        pacienteViewModel.getLoading().observe(this, this::mostrarLoading);
    }

    private void setupRecyclerView() {
        pacienteAdapter = new PacienteAdapter();
        pacienteAdapter.setOnPacienteClickListener(this);

        rvPacientes.setLayoutManager(new LinearLayoutManager(this));
        rvPacientes.setAdapter(pacienteAdapter);
    }

    private void setupClickListeners() {
        // Botón guardar
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarPaciente());

        // Botón cancelar
        findViewById(R.id.btnCancelar).setOnClickListener(v -> limpiarFormulario());

        // Botones de filtro
        btnTodos.setOnClickListener(v -> {
            pacienteViewModel.cargarPacientes();
            actualizarBotonActivo(btnTodos);
        });

        btnIESS.setOnClickListener(v -> {
            pacienteViewModel.filtrarPorTipoSeguro(TipoSeguro.IESS);
            actualizarBotonActivo(btnIESS);
        });

        btnPrivado.setOnClickListener(v -> {
            pacienteViewModel.filtrarPorTipoSeguro(TipoSeguro.PRIVADO);
            actualizarBotonActivo(btnPrivado);
        });

        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        // Establecer botón "Todos" como activo por defecto
        actualizarBotonActivo(btnTodos);
    }

    private void mostrarDialogoBusqueda() {
        View view = getLayoutInflater().inflate(R.layout.dialog_busqueda, null);

        RadioGroup rgCampo = view.findViewById(R.id.rgCampoBusqueda);
        TextInputEditText etTermino = view.findViewById(R.id.etTerminoBusqueda);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Buscar Pacientes")
                .setView(view)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String termino = etTermino.getText().toString().trim();
                    String campo = getCampoSeleccionado(rgCampo);

                    if (!TextUtils.isEmpty(termino)) {
                        pacienteViewModel.buscarPacientes(campo, termino);
                        actualizarBotonActivo(btnBuscar);
                    } else {
                        Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String getCampoSeleccionado(RadioGroup rgCampo) {
        int selectedId = rgCampo.getCheckedRadioButtonId();
        if (selectedId == R.id.rbBuscarNombre)
            return "nombre";
        if (selectedId == R.id.rbBuscarApellido)
            return "apellido";
        if (selectedId == R.id.rbBuscarCedula)
            return "cedula";
        return "correo"; // default
    }

    private void actualizarBotonActivo(Button botonActivo) {
        // Resetear todos los botones a estilo outline
        btnTodos.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnIESS.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnPrivado.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnBuscar.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));

        // Resaltar botón activo
        botonActivo.setBackgroundColor(getResources().getColor(R.color.primary, getTheme()));
    }

    private void guardarPaciente() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String cedula = etCedula.getText().toString().trim();

        // Obtener tipo de seguro seleccionado
        TipoSeguro tipoSeguro = getTipoSeguroSeleccionado();

        // Validaciones básicas
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) ||
                TextUtils.isEmpty(correo) || TextUtils.isEmpty(cedula)) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!correo.contains("@")) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        pacienteViewModel.guardarPaciente(nombre, apellido, correo, cedula, tipoSeguro);
    }

    private TipoSeguro getTipoSeguroSeleccionado() {
        int selectedId = rgTipoSeguro.getCheckedRadioButtonId();
        if (selectedId == R.id.rbIESS) {
            return TipoSeguro.IESS;
        } else {
            return TipoSeguro.PRIVADO;
        }
    }

    private void actualizarListaPacientes(List<Paciente> pacientes) {
        pacienteAdapter.actualizarPacientes(pacientes);
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
        rgTipoSeguro.check(R.id.rbIESS); // Volver al default
        etNombre.requestFocus(); // Poner foco en el primer campo
    }

    @Override
    public void onPacienteClick(Paciente paciente) {
        // Cargar datos del paciente para edición
        etNombre.setText(paciente.getNombre());
        etApellido.setText(paciente.getApellido());
        etCorreo.setText(paciente.getCorreo());
        etCedula.setText(paciente.getCedulaString());

        // Seleccionar tipo de seguro
        if (paciente.getTipoSeguro() == TipoSeguro.IESS) {
            rgTipoSeguro.check(R.id.rbIESS);
        } else {
            rgTipoSeguro.check(R.id.rbPrivado);
        }

        Toast.makeText(this, "Paciente seleccionado: " + paciente.getNombre(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPacienteDelete(Paciente paciente) {
        // Mostrar confirmación antes de eliminar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Paciente")
                .setMessage("¿Está seguro que desea eliminar a " + paciente.getNombre() + " " + paciente.getApellido()
                        + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    pacienteViewModel.eliminarPaciente(paciente.getId());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Volver al menú principal
        startActivity(new Intent(this, com.example.hospital.MainActivity.class));
        finish();
    }
}
