package com.example.hospital.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hospital.data.models.Cirugia;
import com.example.hospital.data.models.Medicacion;
import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.models.Terapia;
import com.example.hospital.data.models.Tratamiento;
import com.example.hospital.data.models.TratamientoPaciente;
import com.example.hospital.data.repository.PacienteRepository;
import com.example.hospital.data.repository.TratamientoPacienteRepository;

import java.util.ArrayList;
import java.util.List;

public class TratamientoPacienteViewModel extends AndroidViewModel {

    private final TratamientoPacienteRepository repository;
    private final PacienteRepository pacienteRepository;

    // LiveData para la UI
    private final MutableLiveData<List<TratamientoPaciente>> tratamientos = new MutableLiveData<>();
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<Paciente> pacienteActual = new MutableLiveData<>();

    // Filtros
    private String filtroActual = "TODOS";

    public TratamientoPacienteViewModel(@NonNull Application application) {
        super(application);
        repository = new TratamientoPacienteRepository(application);
        pacienteRepository = new PacienteRepository(application);
    }

    // Métodos para observación desde la UI
    public LiveData<List<TratamientoPaciente>> getTratamientos() {
        return tratamientos;
    }

    public LiveData<String> getMensaje() {
        return mensaje;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Paciente> getPacienteActual() {
        return pacienteActual;
    }

    // Métodos de carga de datos
    public void cargarTodos() {
        loading.setValue(true);
        filtroActual = "TODOS";
        
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simular carga
                List<TratamientoPaciente> lista = repository.cargarTodos();
                
                // Ordenar por fecha descendente
                lista.sort((a, b) -> b.getFechaAsignacion().compareTo(a.getFechaAsignacion()));
                
                tratamientos.postValue(lista);
                mensaje.postValue("Mostrando todos los tratamientos");
            } catch (Exception e) {
                mensaje.postValue("Error al cargar tratamientos: " + e.getMessage());
            } finally {
                loading.postValue(false);
            }
        }).start();
    }

    public void cargarPorPaciente(String correoPaciente) {
        loading.setValue(true);
        filtroActual = "PACIENTE";
        
        new Thread(() -> {
            try {
                Thread.sleep(300); // Simular carga
                
                // Primero validar que el paciente existe
                Paciente paciente = pacienteRepository.buscarPorCorreo(correoPaciente);
                if (paciente == null) {
                    mensaje.postValue("No se encontró un paciente con el correo: " + correoPaciente);
                    tratamientos.postValue(new ArrayList<>());
                    return;
                }
                
                pacienteActual.postValue(paciente);
                
                List<TratamientoPaciente> lista = repository.cargarPorPaciente(correoPaciente);
                
                // Ordenar por fecha descendente
                lista.sort((a, b) -> b.getFechaAsignacion().compareTo(a.getFechaAsignacion()));
                
                tratamientos.postValue(lista);
                mensaje.postValue("Tratamientos de " + paciente.getNombre() + " " + paciente.getApellido());
            } catch (Exception e) {
                mensaje.postValue("Error al cargar tratamientos del paciente: " + e.getMessage());
            } finally {
                loading.postValue(false);
            }
        }).start();
    }

    public void cargarPorEstado(String estado) {
        loading.setValue(true);
        filtroActual = "ESTADO";
        
        new Thread(() -> {
            try {
                Thread.sleep(300);
                List<TratamientoPaciente> lista = repository.cargarPorEstado(estado);
                
                // Ordenar por fecha descendente
                lista.sort((a, b) -> b.getFechaAsignacion().compareTo(a.getFechaAsignacion()));
                
                tratamientos.postValue(lista);
                mensaje.postValue("Tratamientos con estado: " + estado);
            } catch (Exception e) {
                mensaje.postValue("Error al cargar tratamientos por estado: " + e.getMessage());
            } finally {
                loading.postValue(false);
            }
        }).start();
    }

    // Métodos para guardar tratamientos
    public void guardarTratamientoPaciente(String correoPaciente, String tipoTratamiento, 
                                          String nombre, String valor1, String valor2, String valor3) {
        if (correoPaciente == null || correoPaciente.trim().isEmpty()) {
            mensaje.setValue("Debe ingresar el correo del paciente");
            return;
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            mensaje.setValue("Debe ingresar el nombre del tratamiento");
            return;
        }

        new Thread(() -> {
            try {
                // Validar que el paciente existe
                Paciente paciente = pacienteRepository.buscarPorCorreo(correoPaciente);
                if (paciente == null) {
                    mensaje.postValue("No se encontró un paciente con el correo: " + correoPaciente);
                    return;
                }

                // Crear tratamiento según el tipo
                Tratamiento tratamiento = crearTratamiento(tipoTratamiento, nombre, valor1, valor2, valor3);
                if (tratamiento == null) {
                    mensaje.postValue("Error al crear el tratamiento");
                    return;
                }

                // Crear TratamientoPaciente
                TratamientoPaciente tp = new TratamientoPaciente(paciente, tratamiento);
                tp.setObservaciones("Tratamiento agregado desde la aplicación");

                // Guardar
                repository.guardar(tp);
                
                // Actualizar paciente
                paciente.agregarTratamiento(tp);
                pacienteRepository.actualizarPaciente(paciente);

                mensaje.postValue("Tratamiento asignado exitosamente a " + paciente.getNombre() + " " + paciente.getApellido());
                
                // Recargar la lista según el filtro actual
                if ("PACIENTE".equals(filtroActual)) {
                    cargarPorPaciente(correoPaciente);
                } else {
                    cargarTodos();
                }

            } catch (Exception e) {
                mensaje.postValue("Error al guardar tratamiento: " + e.getMessage());
            }
        }).start();
    }

    private Tratamiento crearTratamiento(String tipo, String nombre, String valor1, String valor2, String valor3) {
        try {
            switch (tipo.toUpperCase()) {
                case "MEDICACIÓN":
                case "MEDICACION":
                    // valor1: costo unidad, valor2: veces al día, valor3: días
                    double costoUnidad = Double.parseDouble(valor1);
                    int dias = Integer.parseInt(valor3);
                    return new Medicacion(nombre, dias, costoUnidad);

                case "CIRUGÍA":
                case "CIRUGIA":
                    // valor1: costo total
                    double costoCirugia = Double.parseDouble(valor1);
                    return new Cirugia(nombre, 1, costoCirugia);

                case "TERAPIA":
                    // valor1: costo sesión, valor2: número de sesiones
                    double costoSesion = Double.parseDouble(valor1);
                    int sesiones = Integer.parseInt(valor2);
                    return new Terapia(nombre, sesiones, costoSesion);

                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void eliminarTratamientoPaciente(TratamientoPaciente tratamiento) {
        new Thread(() -> {
            try {
                boolean eliminado = repository.eliminar(tratamiento.getId());
                if (eliminado) {
                    mensaje.postValue("Tratamiento eliminado exitosamente");
                    
                    // Recargar según el filtro actual
                    if ("PACIENTE".equals(filtroActual) && pacienteActual.getValue() != null) {
                        cargarPorPaciente(pacienteActual.getValue().getCorreo());
                    } else if ("ESTADO".equals(filtroActual)) {
                        cargarPorEstado(tratamiento.getEstado());
                    } else {
                        cargarTodos();
                    }
                } else {
                    mensaje.postValue("No se pudo eliminar el tratamiento");
                }
            } catch (Exception e) {
                mensaje.postValue("Error al eliminar tratamiento: " + e.getMessage());
            }
        }).start();
    }

    public void cambiarEstadoTratamiento(TratamientoPaciente tratamiento, String nuevoEstado) {
        new Thread(() -> {
            try {
                tratamiento.setEstado(nuevoEstado);
                repository.guardar(tratamiento);
                mensaje.postValue("Estado actualizado a: " + nuevoEstado);
                
                // Recargar la lista
                if ("PACIENTE".equals(filtroActual) && pacienteActual.getValue() != null) {
                    cargarPorPaciente(pacienteActual.getValue().getCorreo());
                } else if ("ESTADO".equals(filtroActual)) {
                    cargarPorEstado(nuevoEstado);
                } else {
                    cargarTodos();
                }
            } catch (Exception e) {
                mensaje.postValue("Error al cambiar estado: " + e.getMessage());
            }
        }).start();
    }

    // Métodos de utilidad
    public double getCostoTotalTratamientos() {
        List<TratamientoPaciente> lista = tratamientos.getValue();
        if (lista == null) return 0.0;
        
        return lista.stream()
                .filter(tp -> "ACTIVO".equals(tp.getEstado()))
                .mapToDouble(TratamientoPaciente::getCostoTotal)
                .sum();
    }

    public int getTotalTratamientos() {
        List<TratamientoPaciente> lista = tratamientos.getValue();
        return lista != null ? lista.size() : 0;
    }

    public TratamientoPaciente getTratamientoMasCaro() {
        List<TratamientoPaciente> lista = tratamientos.getValue();
        if (lista == null || lista.isEmpty()) return null;
        
        return lista.stream()
                .max((a, b) -> Double.compare(a.getCostoTotal(), b.getCostoTotal()))
                .orElse(null);
    }

    public void buscarTratamientos(String termino) {
        new Thread(() -> {
            try {
                List<TratamientoPaciente> todos = repository.cargarTodos();
                List<TratamientoPaciente> filtrados = new ArrayList<>();
                
                for (TratamientoPaciente tp : todos) {
                    if (tp.getTratamiento().getNombre().toLowerCase().contains(termino.toLowerCase()) ||
                        tp.getPaciente().getNombre().toLowerCase().contains(termino.toLowerCase()) ||
                        tp.getPaciente().getApellido().toLowerCase().contains(termino.toLowerCase()) ||
                        tp.getPaciente().getCorreo().toLowerCase().contains(termino.toLowerCase())) {
                        filtrados.add(tp);
                    }
                }
                
                // Ordenar por fecha descendente
                filtrados.sort((a, b) -> b.getFechaAsignacion().compareTo(a.getFechaAsignacion()));
                
                tratamientos.postValue(filtrados);
                mensaje.postValue("Resultados de búsqueda: " + filtrados.size() + " tratamientos");
                filtroActual = "BUSQUEDA";
            } catch (Exception e) {
                mensaje.postValue("Error en búsqueda: " + e.getMessage());
            }
        }).start();
    }

    public void limpiarFiltros() {
        pacienteActual.postValue(null);
        cargarTodos();
    }
}