package com.example.hospital.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.hospital.data.models.Cita;
import com.example.hospital.data.models.EstadoCita;
import com.example.hospital.data.models.Medico;
import com.example.hospital.data.models.Tratamiento;
import com.example.hospital.data.models.TratamientoPaciente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository para generar reportes del sistema hospitalario
 * Proporciona métodos para los tres tipos principales de reportes:
 * 1. Citas atendidas por especialidad
 * 2. Ingresos totales por tratamientos
 * 3. Historial de tratamientos por pacientes
 */
public class ReporteRepository {
    private static final String TAG = "ReporteRepository";
    
    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final TratamientoPacienteRepository tratamientoPacienteRepository;

    public ReporteRepository(Context context) {
        this.citaRepository = new CitaRepository(context);
        this.medicoRepository = new MedicoRepository(context);
        this.tratamientoRepository = new TratamientoRepository(context);
        this.tratamientoPacienteRepository = new TratamientoPacienteRepository(context);
    }

    /**
     * REPORTE 1: Citas atendidas por especialidad
     * Retorna un mapa con especialidades como clave y cantidad de citas atendidas como valor
     */
    public Map<String, Integer> getCitasAtendidasPorEspecialidad() {
        Map<String, Integer> resultado = new HashMap<>();
        
        try {
            Log.d(TAG, "Iniciando generación de reporte de citas por especialidad...");
            
            // Obtener todas las citas atendidas
            List<Cita> citasAtendidas = citaRepository.getCitasAtendidas();
            Log.d(TAG, "Citas atendidas encontradas: " + citasAtendidas.size());
            
            // Obtener todos los médicos una sola vez para eficiencia
            List<Medico> medicos = medicoRepository.getAllMedicos();
            Log.d(TAG, "Médicos encontrados: " + medicos.size());
            
            // Agrupar por especialidad del médico
            for (Cita cita : citasAtendidas) {
                try {
                    // Obtener el médico de la cita
                    for (Medico medico : medicos) {
                        if (medico.getCorreo().equals(cita.getMedico())) {
                            String especialidad = medico.getEspecialidad();
                            if (especialidad != null && !especialidad.trim().isEmpty()) {
                                // Incrementar contador para esa especialidad
                                resultado.put(especialidad, resultado.getOrDefault(especialidad, 0) + 1);
                            }
                            break; // Ya encontramos el médico, pasamos a la siguiente cita
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando cita ID: " + cita.getIdCita(), e);
                }
            }
            
            Log.d(TAG, "Reporte de citas por especialidad generado: " + resultado.size() + " especialidades");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al generar reporte de citas por especialidad", e);
            resultado.put("Error", 0); // Devolver algo para evitar null
        }
        
        return resultado;
    }

    /**
     * REPORTE 1 (FILTRADO): Citas atendidas por una especialidad específica
     */
    public List<Cita> getCitasAtendidasPorEspecialidad(String especialidad) {
        List<Cita> resultado = new ArrayList<>();
        
        try {
            List<Cita> citasAtendidas = citaRepository.getCitasAtendidas();
            
            // Filtrar por especialidad específica
            for (Cita cita : citasAtendidas) {
                List<Medico> medicos = medicoRepository.getAllMedicos();
                for (Medico medico : medicos) {
                    if (medico.getCorreo().equals(cita.getMedico()) && 
                        medico.getEspecialidad().equalsIgnoreCase(especialidad)) {
                        resultado.add(cita);
                        break;
                    }
                }
            }
            
            Log.d(TAG, "Citas encontradas para especialidad " + especialidad + ": " + resultado.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error al filtrar citas por especialidad: " + especialidad, e);
        }
        
        return resultado;
    }

    /**
     * REPORTE 2: Ingresos totales por tratamientos
     * Retorna un mapa con tipos de tratamiento como clave y ingresos totales como valor
     */
    public Map<String, Double> getIngresosTotalesPorTratamientos() {
        Map<String, Double> resultado = new HashMap<>();
        
        try {
            // Obtener todos los tratamientos asignados a pacientes
            List<TratamientoPaciente> tratamientosAsignados = tratamientoPacienteRepository.cargarTodos();
            
            // Calcular ingresos por tipo de tratamiento
            for (TratamientoPaciente tp : tratamientosAsignados) {
                String tipo = tp.getTratamiento().getTipo();
                double costo = tp.getTratamiento().calcularCosto();
                
                resultado.put(tipo, resultado.getOrDefault(tipo, 0.0) + costo);
            }
            
            Log.d(TAG, "Reporte de ingresos por tratamientos generado: " + resultado.size() + " tipos");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al generar reporte de ingresos por tratamientos", e);
        }
        
        return resultado;
    }

    /**
     * REPORTE 2 (FILTRADO): Ingresos por tipo específico de tratamiento
     */
    public double getIngresosPorTipoTratamiento(String tipo) {
        double total = 0.0;
        
        try {
            List<TratamientoPaciente> tratamientosAsignados = tratamientoPacienteRepository.cargarTodos();
            
            for (TratamientoPaciente tp : tratamientosAsignados) {
                if (tp.getTratamiento().getTipo().equalsIgnoreCase(tipo)) {
                    total += tp.getTratamiento().calcularCosto();
                }
            }
            
            Log.d(TAG, "Ingresos para tipo " + tipo + ": $" + total);
            
        } catch (Exception e) {
            Log.e(TAG, "Error al calcular ingresos por tipo: " + tipo, e);
        }
        
        return total;
    }

    /**
     * REPORTE 3: Historial de tratamientos por paciente
     * Retorna un mapa con pacientes como clave y lista de tratamientos como valor
     */
    public Map<String, List<TratamientoPaciente>> getHistorialTratamientosPorPaciente() {
        Map<String, List<TratamientoPaciente>> resultado = new HashMap<>();
        
        try {
            List<TratamientoPaciente> tratamientosAsignados = tratamientoPacienteRepository.cargarTodos();
            
            // Agrupar por paciente
            for (TratamientoPaciente tp : tratamientosAsignados) {
                String clavePaciente = tp.getPaciente().getNombre() + " " + tp.getPaciente().getApellido() + 
                                     " (" + tp.getPaciente().getCorreo() + ")";
                
                resultado.computeIfAbsent(clavePaciente, k -> new ArrayList<>()).add(tp);
            }
            
            Log.d(TAG, "Historial de tratamientos generado: " + resultado.size() + " pacientes");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al generar historial de tratamientos por paciente", e);
        }
        
        return resultado;
    }

    /**
     * REPORTE 3 (FILTRADO): Historial de tratamientos para un paciente específico
     */
    public List<TratamientoPaciente> getHistorialTratamientosPorPaciente(String correoPaciente) {
        List<TratamientoPaciente> resultado = new ArrayList<>();
        
        try {
            resultado = tratamientoPacienteRepository.cargarPorPaciente(correoPaciente);
            Log.d(TAG, "Tratamientos encontrados para paciente " + correoPaciente + ": " + resultado.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener historial del paciente: " + correoPaciente, e);
        }
        
        return resultado;
    }

    /**
     * Métodos utilitarios para reportes
     */

    /**
     * Obtiene todas las especialidades disponibles
     */
    public List<String> getEspecialidadesDisponibles() {
        List<String> especialidades = new ArrayList<>();
        
        try {
            List<Medico> medicos = medicoRepository.getAllMedicos();
            especialidades = medicos.stream()
                    .map(Medico::getEspecialidad)
                    .distinct()
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener especialidades disponibles", e);
        }
        
        return especialidades;
    }

    /**
     * Obtiene todos los tipos de tratamiento disponibles
     */
    public List<String> getTiposTratamientoDisponibles() {
        List<String> tipos = new ArrayList<>();
        
        try {
            List<Tratamiento> tratamientos = tratamientoRepository.getAllTratamientos();
            tipos = tratamientos.stream()
                    .map(Tratamiento::getTipo)
                    .distinct()
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener tipos de tratamiento disponibles", e);
        }
        
        return tipos;
    }

    /**
     * Obtiene todos los pacientes que tienen tratamientos asignados
     */
    public List<String> getPacientesConTratamientos() {
        try {
            return tratamientoPacienteRepository.getPacientesConTratamientos();
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener pacientes con tratamientos", e);
            return new ArrayList<>();
        }
    }

    /**
     * Estadísticas generales del sistema
     */
    public Map<String, Object> getEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Citas
            List<Cita> todasCitas = citaRepository.getAllCitas();
            long citasAtendidas = todasCitas.stream()
                    .filter(c -> c.getEstadoCita() == EstadoCita.ATENDIDA)
                    .count();
            long citasProgramadas = todasCitas.stream()
                    .filter(c -> c.getEstadoCita() == EstadoCita.PROGRAMADA)
                    .count();
            long citasCanceladas = todasCitas.stream()
                    .filter(c -> c.getEstadoCita() == EstadoCita.CANCELADA)
                    .count();

            // Tratamientos
            List<TratamientoPaciente> tratamientosAsignados = tratamientoPacienteRepository.cargarTodos();
            double ingresosTotales = tratamientosAsignados.stream()
                    .mapToDouble(tp -> tp.getTratamiento().calcularCosto())
                    .sum();

            estadisticas.put("totalCitas", todasCitas.size());
            estadisticas.put("citasAtendidas", (int) citasAtendidas);
            estadisticas.put("citasProgramadas", (int) citasProgramadas);
            estadisticas.put("citasCanceladas", (int) citasCanceladas);
            estadisticas.put("totalTratamientosAsignados", tratamientosAsignados.size());
            estadisticas.put("ingresosTotales", ingresosTotales);
            estadisticas.put("totalMedicos", medicoRepository.getAllMedicos().size());
            
            // Para pacientes, usamos los pacientes que tienen tratamientos asignados
            List<String> pacientesConTratamientos = getPacientesConTratamientos();
            estadisticas.put("totalPacientes", pacientesConTratamientos.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error al generar estadísticas generales", e);
            // En caso de error, devolver valores por defecto
            estadisticas.put("totalCitas", 0);
            estadisticas.put("citasAtendidas", 0);
            estadisticas.put("citasProgramadas", 0);
            estadisticas.put("citasCanceladas", 0);
            estadisticas.put("totalTratamientosAsignados", 0);
            estadisticas.put("ingresosTotales", 0.0);
            estadisticas.put("totalMedicos", 0);
            estadisticas.put("totalPacientes", 0);
        }
        
        return estadisticas;
    }
}