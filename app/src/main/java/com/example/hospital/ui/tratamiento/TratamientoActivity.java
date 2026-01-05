package com.example.hospital.ui.tratamiento;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.example.hospital.data.models.Cirugia;
import com.example.hospital.data.models.Medicacion;
import com.example.hospital.data.models.Terapia;
import com.example.hospital.data.models.Tratamiento;
import com.example.hospital.viewmodel.TratamientoViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class TratamientoActivity extends AppCompatActivity implements TratamientoAdapter.OnTratamientoClickListener {

    // UI Components
    private TextInputEditText etNombre;
    private TextInputEditText etDuracion;
    private TextInputEditText etPrecio;
    private Spinner spTipo;
    private RecyclerView rvTratamientos;
    private ProgressBar progressBar;
    private TextView tvMensaje;

    // Botones de filtro
    private Button btnTodos, btnCirugias, btnMedicaciones, btnTerapias, btnBuscar;

    // ViewModel y Adapter
    private TratamientoViewModel tratamientoViewModel;
    private TratamientoAdapter tratamientoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tratamiento);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupSpinner();
        setupClickListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etDuracion = findViewById(R.id.etDuracion);
        etPrecio = findViewById(R.id.etPrecio);
        spTipo = findViewById(R.id.spTipo);
        rvTratamientos = findViewById(R.id.rvTratamientos);
        progressBar = findViewById(R.id.progressBar);

        // Botones de filtro
        btnTodos = findViewById(R.id.btnTodos);
        btnCirugias = findViewById(R.id.btnCirugias);
        btnMedicaciones = findViewById(R.id.btnMedicaciones);
        btnTerapias = findViewById(R.id.btnTerapias);
        btnBuscar = findViewById(R.id.btnBuscar);
    }

    private void setupViewModel() {
        tratamientoViewModel = new ViewModelProvider(this).get(TratamientoViewModel.class);

        // Observar LiveData
        tratamientoViewModel.getTratamientos().observe(this, this::actualizarListaTratamientos);
        tratamientoViewModel.getMensaje().observe(this, this::mostrarMensaje);
        tratamientoViewModel.getLoading().observe(this, this::mostrarLoading);
    }

    private void setupRecyclerView() {
        tratamientoAdapter = new TratamientoAdapter();
        tratamientoAdapter.setOnTratamientoClickListener(this);

        rvTratamientos.setLayoutManager(new LinearLayoutManager(this));
        rvTratamientos.setAdapter(tratamientoAdapter);
    }

    private void setupSpinner() {
        // Spinner de tipos de tratamiento
        String[] tipos = {"Cirugía", "Medicación", "Terapia"};
        ArrayAdapter<String> tiposAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, tipos);
        tiposAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(tiposAdapter);

        // Configurar listener para ajustar labels según el tipo
        spTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarLabelsSegunTipo(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void actualizarLabelsSegunTipo(int posicion) {
        String[] tipoDias = {"horas", "días", "sesiones"};
        String[] tipoPrecio = {"por hora", "por día", "por sesión"};
        
        // Aquí podrías actualizar los hint de los campos si quisieras
        // pero por ahora lo dejo como está
    }

    private void setupClickListeners() {
        // Botón guardar
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarTratamiento());

        // Botón cancelar
        findViewById(R.id.btnCancelar).setOnClickListener(v -> limpiarFormulario());

        // Botones de filtro
        btnTodos.setOnClickListener(v -> {
            tratamientoViewModel.cargarTratamientos();
            actualizarBotonActivo(btnTodos);
        });

        btnCirugias.setOnClickListener(v -> {
            tratamientoViewModel.cargarCirugias();
            actualizarBotonActivo(btnCirugias);
        });

        btnMedicaciones.setOnClickListener(v -> {
            tratamientoViewModel.cargarMedicaciones();
            actualizarBotonActivo(btnMedicaciones);
        });

        btnTerapias.setOnClickListener(v -> {
            tratamientoViewModel.cargarTerapias();
            actualizarBotonActivo(btnTerapias);
        });

        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        // Botones de estadísticas
        findViewById(R.id.btnEstadisticas).setOnClickListener(v -> mostrarEstadisticas());

        // Establecer botón "Todos" como activo por defecto
        actualizarBotonActivo(btnTodos);
    }

    private void mostrarDialogoBusqueda() {
        View view = getLayoutInflater().inflate(R.layout.dialog_busqueda_tratamiento, null);

        Spinner spCampo = view.findViewById(R.id.spCampoBusqueda);
        TextInputEditText etTermino = view.findViewById(R.id.etTerminoBusqueda);

        // Configurar spinner de campos
        String[] campos = {"Nombre", "Precio máximo", "Duración máxima"};
        ArrayAdapter<String> camposAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, campos);
        camposAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCampo.setAdapter(camposAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Buscar Tratamientos")
                .setView(view)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String termino = etTermino.getText().toString().trim();
                    String campo = (String) spCampo.getSelectedItem();

                    if (!TextUtils.isEmpty(termino)) {
                        switch (campo) {
                            case "Nombre":
                                tratamientoViewModel.buscarTratamientos(termino);
                                break;
                            case "Precio máximo":
                                try {
                                    double precio = Double.parseDouble(termino);
                                    tratamientoViewModel.filtrarPorPrecioMaximo(precio);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(this, "Ingrese un precio válido", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                break;
                            case "Duración máxima":
                                try {
                                    int duracion = Integer.parseInt(termino);
                                    tratamientoViewModel.filtrarPorDuracionMaxima(duracion);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(this, "Ingrese una duración válida", Toast.LENGTH_SHORT).show();
                                    return;
                                }
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

    private void mostrarEstadisticas() {
        int total = tratamientoViewModel.getTotalTratamientos();
        double promedio = tratamientoViewModel.getCostoPromedio();
        Tratamiento masCaro = tratamientoViewModel.getTratamientoMasCaro();

        StringBuilder stats = new StringBuilder();
        stats.append("📊 ESTADÍSTICAS DE TRATAMIENTOS\n\n");
        stats.append("📋 Total de tratamientos: ").append(total).append("\n");
        stats.append("💰 Costo promedio: $").append(String.format("%.2f", promedio)).append("\n");
        
        if (masCaro != null) {
            stats.append("💎 Tratamiento más caro:\n");
            stats.append("   ").append(masCaro.getNombre()).append("\n");
            stats.append("   Costo: $").append(String.format("%.2f", masCaro.calcularCosto()));
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Estadísticas")
                .setMessage(stats.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void actualizarBotonActivo(Button botonActivo) {
        // Resetear todos los botones a estilo outline
        btnTodos.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnCirugias.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnMedicaciones.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnTerapias.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        btnBuscar.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));

        // Resaltar botón activo
        botonActivo.setBackgroundColor(getResources().getColor(R.color.primary, getTheme()));
    }

    private void guardarTratamiento() {
        String nombre = etNombre.getText().toString().trim();
        String duracionStr = etDuracion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String tipo = (String) spTipo.getSelectedItem();

        // Validaciones básicas
        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "Ingrese el nombre del tratamiento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(duracionStr)) {
            Toast.makeText(this, "Ingrese la duración", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(precioStr)) {
            Toast.makeText(this, "Ingrese el precio", Toast.LENGTH_SHORT).show();
            return;
        }

        tratamientoViewModel.guardarTratamiento(nombre, duracionStr, precioStr, tipo, false);
    }

    private void actualizarListaTratamientos(List<Tratamiento> tratamientos) {
        tratamientoAdapter.actualizarTratamientos(tratamientos);
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
        etDuracion.setText("");
        etPrecio.setText("");
        spTipo.setSelection(0);
        etNombre.requestFocus(); // Poner foco en el primer campo
    }

    @Override
    public void onTratamientoClick(Tratamiento tratamiento) {
        // Cargar datos del tratamiento para edición
        etNombre.setText(tratamiento.getNombre());
        etDuracion.setText(String.valueOf(tratamiento.getDuracion()));
        etPrecio.setText(String.valueOf(tratamiento.getPrecio()));

        // Seleccionar tipo según el tipo de tratamiento
        if (tratamiento instanceof Cirugia) {
            spTipo.setSelection(0); // "Cirugía"
        } else if (tratamiento instanceof Medicacion) {
            spTipo.setSelection(1); // "Medicación"
        } else if (tratamiento instanceof Terapia) {
            spTipo.setSelection(2); // "Terapia"
        }

        tratamientoViewModel.setTratamientoActual(tratamiento);
        Toast.makeText(this, "Tratamiento seleccionado: " + tratamiento.getNombre(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTratamientoDelete(Tratamiento tratamiento) {
        // Mostrar confirmación antes de eliminar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Tratamiento")
                .setMessage("¿Está seguro que desea eliminar el tratamiento: " + tratamiento.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    tratamientoViewModel.eliminarTratamiento(tratamiento);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onTratamientoShowCost(Tratamiento tratamiento) {
        double costo = tratamiento.calcularCosto();
        String mensaje = "Costo total del tratamiento:\n\n";
        mensaje += "📋 " + tratamiento.getNombre() + "\n";
        mensaje += "⏰ Duración: " + tratamiento.getDuracion();
        
        // Agregar etiqueta específica según tipo
        if (tratamiento instanceof Cirugia) {
            mensaje += " horas\n";
        } else if (tratamiento instanceof Medicacion) {
            mensaje += " días\n";
        } else if (tratamiento instanceof Terapia) {
            mensaje += " sesiones\n";
        }
        
        mensaje += "💰 Costo base: $" + com.example.hospital.data.models.Tratamiento.getCostoBase() + "\n";
        mensaje += "💵 Precio unitario: $" + tratamiento.getPrecio() + "\n";
        mensaje += "💳 Costo total: $" + String.format("%.2f", costo);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Calcular Costo")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Volver al menú principal
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}