package com.example.hospital.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hospital.data.models.Cita;
import com.example.hospital.data.models.TratamientoPaciente;
import com.example.hospital.data.repository.ReporteRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel para manejar la lógica de los reportes del sistema hospitalario
 * Administra los tres tipos principales de reportes y sus filtros
 */
public class ReporteViewModel extends AndroidViewModel {
    private static final String TAG = "ReporteViewModel";
    
    private final ReporteRepository reporteRepository;
    
    // LiveData para los diferentes reportes
    private final MutableLiveData<Map<String, Integer>> citasPorEspecialidad;
    private final MutableLiveData<List<Cita>> citasFiltradasPorEspecialidad;
    private final MutableLiveData<Map<String, Double>> ingresosPorTratamientos;
    private final MutableLiveData<Double> ingresosFiltradosPorTipo;
    private final MutableLiveData<Map<String, List<TratamientoPaciente>>> historialPorPaciente;
    private final MutableLiveData<List<TratamientoPaciente>> historialFiltradoPorPaciente;
    
    // LiveData para datos de filtros
    private final MutableLiveData<List<String>> especialidadesDisponibles;
    private final MutableLiveData<List<String>> tiposTratamientoDisponibles;
    private final MutableLiveData<List<String>> pacientesConTratamientos;
    
    // LiveData para estadísticas generales
    private final MutableLiveData<Map<String, Object>> estadisticasGenerales;
    
    // LiveData comunes
    private final MutableLiveData<String> mensaje;
    private final MutableLiveData<Boolean> loading;

    public ReporteViewModel(Application application) {
        super(application);
        this.reporteRepository = new ReporteRepository(application.getApplicationContext());
        
        // Inicializar LiveData
        this.citasPorEspecialidad = new MutableLiveData<>();
        this.citasFiltradasPorEspecialidad = new MutableLiveData<>();
        this.ingresosPorTratamientos = new MutableLiveData<>();
        this.ingresosFiltradosPorTipo = new MutableLiveData<>();
        this.historialPorPaciente = new MutableLiveData<>();
        this.historialFiltradoPorPaciente = new MutableLiveData<>();
        
        this.especialidadesDisponibles = new MutableLiveData<>();
        this.tiposTratamientoDisponibles = new MutableLiveData<>();
        this.pacientesConTratamientos = new MutableLiveData<>();
        this.estadisticasGenerales = new MutableLiveData<>();
        
        this.mensaje = new MutableLiveData<>();
        this.loading = new MutableLiveData<>();
        
        // Cargar datos iniciales
        cargarDatosIniciales();
    }

    // Getters para LiveData
    public LiveData<Map<String, Integer>> getCitasPorEspecialidad() {
        return citasPorEspecialidad;
    }

    public LiveData<List<Cita>> getCitasFiltradasPorEspecialidad() {
        return citasFiltradasPorEspecialidad;
    }

    public LiveData<Map<String, Double>> getIngresosPorTratamientos() {
        return ingresosPorTratamientos;
    }

    public LiveData<Double> getIngresosFiltradosPorTipo() {
        return ingresosFiltradosPorTipo;
    }

    public LiveData<Map<String, List<TratamientoPaciente>>> getHistorialPorPaciente() {
        return historialPorPaciente;
    }

    public LiveData<List<TratamientoPaciente>> getHistorialFiltradoPorPaciente() {
        return historialFiltradoPorPaciente;
    }

    public LiveData<List<String>> getEspecialidadesDisponibles() {
        return especialidadesDisponibles;
    }

    public LiveData<List<String>> getTiposTratamientoDisponibles() {
        return tiposTratamientoDisponibles;
    }

    public LiveData<List<String>> getPacientesConTratamientos() {
        return pacientesConTratamientos;
    }

    public LiveData<Map<String, Object>> getEstadisticasGenerales() {
        return estadisticasGenerales;
    }

    public LiveData<String> getMensaje() {
        return mensaje;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * MÉTODOS PARA REPORTE 1: CITAS ATENDIDAS POR ESPECIALIDAD
     */
    public void cargarCitasAtendidasPorEspecialidad() {
        loading.setValue(true);
        
        try {
            Map<String, Integer> datos = reporteRepository.getCitasAtendidasPorEspecialidad();
            citasPorEspecialidad.setValue(datos);
            
            int totalCitas = datos.values().stream().mapToInt(Integer::intValue).sum();
            mensaje.setValue("Reporte generado: " + totalCitas + " citas atendidas en " + 
                            datos.size() + " especialidades");
            
            Log.d(TAG, "Reporte de citas por especialidad cargado exitosamente");
            
        } catch (Exception e) {
            mensaje.setValue("Error al cargar reporte de citas por especialidad: " + e.getMessage());
            Log.e(TAG, "Error cargando citas por especialidad", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarCitasPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.trim().isEmpty()) {
            mensaje.setValue("Por favor seleccione una especialidad válida");
            return;
        }

        loading.setValue(true);
        
        try {
            List<Cita> citas = reporteRepository.getCitasAtendidasPorEspecialidad(especialidad);
            citasFiltradasPorEspecialidad.setValue(citas);
            
            mensaje.setValue("Se encontraron " + citas.size() + " citas atendidas para la especialidad: " + 
                            especialidad);
            
            Log.d(TAG, "Citas filtradas por especialidad: " + especialidad);
            
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar citas por especialidad: " + e.getMessage());
            Log.e(TAG, "Error filtrando citas por especialidad", e);
        } finally {
            loading.setValue(false);
        }
    }

    /**
     * MÉTODOS PARA REPORTE 2: INGRESOS TOTALES POR TRATAMIENTOS
     */
    public void cargarIngresosTotalesPorTratamientos() {
        loading.setValue(true);
        
        try {
            Map<String, Double> datos = reporteRepository.getIngresosTotalesPorTratamientos();
            ingresosPorTratamientos.setValue(datos);
            
            double totalIngresos = datos.values().stream().mapToDouble(Double::doubleValue).sum();
            mensaje.setValue("Ingresos totales: $" + String.format("%.2f", totalIngresos) + 
                            " en " + datos.size() + " tipos de tratamiento");
            
            Log.d(TAG, "Reporte de ingresos por tratamientos cargado exitosamente");
            
        } catch (Exception e) {
            mensaje.setValue("Error al cargar reporte de ingresos por tratamientos: " + e.getMessage());
            Log.e(TAG, "Error cargando ingresos por tratamientos", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarIngresosPorTipoTratamiento(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            cargarIngresosTotalesPorTratamientos(); // Si no hay filtro, mostrar todos
            return;
        }

        loading.setValue(true);
        
        try {
            double ingresos = reporteRepository.getIngresosPorTipoTratamiento(tipo);
            ingresosFiltradosPorTipo.setValue(ingresos);
            
            mensaje.setValue("Ingresos por " + tipo + ": $" + String.format("%.2f", ingresos));
            
            Log.d(TAG, "Ingresos filtrados por tipo: " + tipo);
            
        } catch (Exception e) {
            mensaje.setValue("Error al calcular ingresos por tipo: " + e.getMessage());
            Log.e(TAG, "Error calculando ingresos por tipo", e);
        } finally {
            loading.setValue(false);
        }
    }

    /**
     * MÉTODOS PARA REPORTE 3: HISTORIAL DE TRATAMIENTOS POR PACIENTES
     */
    public void cargarHistorialTratamientosPorPaciente() {
        loading.setValue(true);
        
        try {
            Map<String, List<TratamientoPaciente>> datos = reporteRepository.getHistorialTratamientosPorPaciente();
            historialPorPaciente.setValue(datos);
            
            int totalTratamientos = datos.values().stream().mapToInt(List::size).sum();
            mensaje.setValue("Historial cargado: " + totalTratamientos + " tratamientos para " + 
                            datos.size() + " pacientes");
            
            Log.d(TAG, "Historial de tratamientos por paciente cargado exitosamente");
            
        } catch (Exception e) {
            mensaje.setValue("Error al cargar historial de tratamientos: " + e.getMessage());
            Log.e(TAG, "Error cargando historial de tratamientos", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarHistorialPorPaciente(String correoPaciente) {
        if (correoPaciente == null || correoPaciente.trim().isEmpty() || 
            !correoPaciente.contains("@")) {
            mensaje.setValue("Por favor seleccione un paciente válido");
            return;
        }

        loading.setValue(true);
        
        try {
            List<TratamientoPaciente> historial = reporteRepository.getHistorialTratamientosPorPaciente(correoPaciente);
            historialFiltradoPorPaciente.setValue(historial);
            
            mensaje.setValue("Se encontraron " + historial.size() + " tratamientos para el paciente: " + 
                            correoPaciente);
            
            Log.d(TAG, "Historial filtrado por paciente: " + correoPaciente);
            
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar historial por paciente: " + e.getMessage());
            Log.e(TAG, "Error filtrando historial por paciente", e);
        } finally {
            loading.setValue(false);
        }
    }

    /**
     * MÉTODOS PARA CARGAR DATOS DE FILTROS
     */
    public void cargarDatosIniciales() {
        loading.setValue(true);
        
        try {
            Log.d(TAG, "Iniciando carga de datos iniciales...");
            
            // Cargar datos para los filtros - con manejo de errores individual
            try {
                especialidadesDisponibles.setValue(reporteRepository.getEspecialidadesDisponibles());
                Log.d(TAG, "Especialidades cargadas");
            } catch (Exception e) {
                Log.e(TAG, "Error cargando especialidades", e);
                especialidadesDisponibles.setValue(new ArrayList<>());
            }
            
            try {
                tiposTratamientoDisponibles.setValue(reporteRepository.getTiposTratamientoDisponibles());
                Log.d(TAG, "Tipos de tratamiento cargados");
            } catch (Exception e) {
                Log.e(TAG, "Error cargando tipos de tratamiento", e);
                tiposTratamientoDisponibles.setValue(new ArrayList<>());
            }
            
            try {
                pacientesConTratamientos.setValue(reporteRepository.getPacientesConTratamientos());
                Log.d(TAG, "Pacientes con tratamientos cargados");
            } catch (Exception e) {
                Log.e(TAG, "Error cargando pacientes con tratamientos", e);
                pacientesConTratamientos.setValue(new ArrayList<>());
            }
            
            try {
                estadisticasGenerales.setValue(reporteRepository.getEstadisticasGenerales());
                Log.d(TAG, "Estadísticas generales cargadas");
            } catch (Exception e) {
                Log.e(TAG, "Error cargando estadísticas generales", e);
                estadisticasGenerales.setValue(new HashMap<>());
            }
            
            // Cargar reportes iniciales - con manejo de errores
            try {
                cargarCitasAtendidasPorEspecialidad();
            } catch (Exception e) {
                Log.e(TAG, "Error en carga de citas por especialidad", e);
            }
            
            try {
                cargarIngresosTotalesPorTratamientos();
            } catch (Exception e) {
                Log.e(TAG, "Error en carga de ingresos por tratamientos", e);
            }
            
            try {
                cargarHistorialTratamientosPorPaciente();
            } catch (Exception e) {
                Log.e(TAG, "Error en carga de historial por paciente", e);
            }
            
            Log.d(TAG, "Datos iniciales cargados exitosamente");
            mensaje.setValue("Reportes del sistema hospitalario cargados");
            
        } catch (Exception e) {
            Log.e(TAG, "Error general cargando datos iniciales", e);
            mensaje.setValue("Error al cargar datos iniciales: " + e.getMessage());
        } finally {
            loading.setValue(false);
        }
    }

    /**
     * MÉTODOS UTILITARIOS
     */
    public void limpiarFiltros() {
        loading.setValue(true);
        
        try {
            citasFiltradasPorEspecialidad.setValue(null);
            ingresosFiltradosPorTipo.setValue(null);
            historialFiltradoPorPaciente.setValue(null);
            
            mensaje.setValue("Filtros limpiados. Mostrando reportes completos.");
            
            Log.d(TAG, "Filtros limpiados exitosamente");
            
        } catch (Exception e) {
            mensaje.setValue("Error al limpiar filtros: " + e.getMessage());
            Log.e(TAG, "Error limpiando filtros", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void recargarTodosLosReportes() {
        mensaje.setValue("Recargando todos los reportes...");
        cargarDatosIniciales();
    }

    public void limpiarMensaje() {
        mensaje.setValue("");
    }

    /**
     * Métodos específicos para formateo de datos
     */
    public String formatearMoneda(double cantidad) {
        return String.format("$%.2f", cantidad);
    }

    public String generarResumenReportes() {
        Map<String, Object> estadisticas = estadisticasGenerales.getValue();
        if (estadisticas == null) {
            return "📊 RESUMEN DEL SISTEMA\n\nCargando estadísticas...";
        }

        try {
            StringBuilder resumen = new StringBuilder();
            resumen.append("📊 RESUMEN DEL SISTEMA\n\n");
            resumen.append("🏥 CITAS:\n");
            resumen.append("  • Total: ").append(estadisticas.getOrDefault("totalCitas", 0)).append("\n");
            resumen.append("  • Atendidas: ").append(estadisticas.getOrDefault("citasAtendidas", 0)).append("\n");
            resumen.append("  • Programadas: ").append(estadisticas.getOrDefault("citasProgramadas", 0)).append("\n");
            resumen.append("  • Canceladas: ").append(estadisticas.getOrDefault("citasCanceladas", 0)).append("\n\n");
            
            resumen.append("💊 TRATAMIENTOS:\n");
            resumen.append("  • Asignados: ").append(estadisticas.getOrDefault("totalTratamientosAsignados", 0)).append("\n");
            Double ingresos = (Double) estadisticas.getOrDefault("ingresosTotales", 0.0);
            resumen.append("  • Ingresos totales: ").append(formatearMoneda(ingresos)).append("\n\n");
            
            resumen.append("👥 PERSONAL:\n");
            resumen.append("  • Médicos: ").append(estadisticas.getOrDefault("totalMedicos", 0)).append("\n");
            resumen.append("  • Pacientes: ").append(estadisticas.getOrDefault("totalPacientes", 0)).append("\n");
            
            return resumen.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error generando resumen", e);
            return "📊 RESUMEN DEL SISTEMA\n\nError al cargar estadísticas";
        }
    }
}