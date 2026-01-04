package com.example.hospital.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.models.TipoSeguro;
import com.example.hospital.data.repository.PacienteRepository;

import java.util.List;
import java.util.Optional;

public class PacienteViewModel extends AndroidViewModel {
    private static final String TAG = "PacienteViewModel";
    
    private final PacienteRepository pacienteRepository;
    private final MutableLiveData<List<Paciente>> pacientes;
    private final MutableLiveData<String> mensaje;
    private final MutableLiveData<Boolean> loading;
    private Paciente pacienteActual;

    public PacienteViewModel(Application application) {
        super(application);
        this.pacienteRepository = new PacienteRepository(application.getApplicationContext());
        this.pacientes = new MutableLiveData<>();
        this.mensaje = new MutableLiveData<>();
        this.loading = new MutableLiveData<>();
        
        cargarPacientes();
    }

    public LiveData<List<Paciente>> getPacientes() {
        return pacientes;
    }
    
    public LiveData<String> getMensaje() {
        return mensaje;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void cargarPacientes() {
        loading.setValue(true);
        try {
            List<Paciente> lista = pacienteRepository.getAllPacientes();
            pacientes.setValue(lista);
            mensaje.setValue("pacientes cargados: " + lista.size());
            Log.d(TAG, "Cargados " + lista.size() + " pacientes");
        } catch (Exception e) {
            mensaje.setValue("Error al cargar pacientes: " + e.getMessage());
            Log.e(TAG, "Error cargando pacientes", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void guardarPaciente(String nombre, String apellido, String correo, 
                           String cedula, TipoSeguro tipoSeguro) {
        guardarPaciente(nombre, apellido, correo, cedula, tipoSeguro, false);
    }

    public void guardarPaciente(String nombre, String apellido, String correo, 
                           String cedula, TipoSeguro tipoSeguro, boolean esEdicion) {
        loading.setValue(true);
        
        if (nombre == null || nombre.trim().isEmpty()) {
            mensaje.setValue("Error: El nombre es obligatorio");
            loading.setValue(false);
            return;
        }
        
        if (apellido == null || apellido.trim().isEmpty()) {
            mensaje.setValue("Error: El apellido es obligatorio");
            loading.setValue(false);
            return;
        }
        
        if (correo == null || !correo.contains("@")) {
            mensaje.setValue("Error: El correo no es válido");
            loading.setValue(false);
            return;
        }
        
        if (cedula == null || cedula.trim().isEmpty()) {
            mensaje.setValue("Error: La cédula es obligatoria");
            loading.setValue(false);
            return;
        }
        
        try {
            if (esEdicion && pacienteActual != null) {
                pacienteActual.setNombre(nombre.trim());
                pacienteActual.setApellido(apellido.trim());
                pacienteActual.setCorreo(correo.trim().toLowerCase());
                pacienteActual.setTipoSeguro(tipoSeguro);
                
                boolean resultado = pacienteRepository.actualizarPaciente(pacienteActual);
                
                if (resultado) {
                    mensaje.setValue("Paciente actualizado exitosamente");
                    cargarPacientes();
                } else {
                    mensaje.setValue("No se pudo actualizar el paciente");
                }
            } else {
                Paciente nuevoPaciente = new Paciente(
                    0, nombre.trim(), apellido.trim(), 
                    correo.trim().toLowerCase(), cedula.trim(), tipoSeguro
                );
                
                boolean resultado = pacienteRepository.guardarPaciente(nuevoPaciente);
                
                if (resultado) {
                    mensaje.setValue("Paciente guardado exitosamente");
                    cargarPacientes();
                } else {
                    mensaje.setValue("El paciente ya está registrado");
                }
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al guardar paciente: " + e.getMessage());
            Log.e(TAG, "Error guardando paciente", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorTipoSeguro(TipoSeguro tipoSeguro) {
        loading.setValue(true);
        try {
            List<Paciente> filtrados = pacienteRepository.getPacientesPorTipoSeguro(tipoSeguro);
            pacientes.setValue(filtrados);
            mensaje.setValue("Pacientes con seguro " + tipoSeguro.name() + ": " + filtrados.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por seguro: " + e.getMessage());
            Log.e(TAG, "Error filtrando por seguro", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void buscarPacientes(String campo, String termino) {
        loading.setValue(true);
        try {
            List<Paciente> resultados = pacienteRepository.buscarPacientes(campo, termino);
            pacientes.setValue(resultados);
            mensaje.setValue("Resultados de búsqueda: " + resultados.size() + " pacientes");
        } catch (Exception e) {
            mensaje.setValue("Error en búsqueda: " + e.getMessage());
            Log.e(TAG, "Error en búsqueda", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            cargarPacientes();
            return;
        }
        
        loading.setValue(true);
        try {
            Optional<Paciente> encontrado = pacienteRepository.getPacientePorCorreo(correo.trim());
            if (encontrado.isPresent()) {
                pacientes.setValue(List.of(encontrado.get()));
                mensaje.setValue("Paciente encontrado: " + encontrado.get().getNombre());
            } else {
                mensaje.setValue("Paciente no encontrado");
                pacientes.setValue(List.of());
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al buscar paciente: " + e.getMessage());
            Log.e(TAG, "Error buscando paciente", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void eliminarPaciente(int id) {
        loading.setValue(true);
        try {
            boolean resultado = pacienteRepository.eliminarPaciente(id);
            
            if (resultado) {
                mensaje.setValue("Paciente eliminado exitosamente");
                cargarPacientes();
            } else {
                mensaje.setValue("No se pudo eliminar el paciente");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al eliminar paciente: " + e.getMessage());
            Log.e(TAG, "Error eliminando paciente", e);
        } finally {
            loading.setValue(false);
        }
    }
    
    public void eliminarPaciente(String cedula) {
        loading.setValue(true);
        try {
            Optional<Paciente> pacienteOpt = pacienteRepository.getPacientePorCedula(cedula);
            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                boolean resultado = pacienteRepository.eliminarPaciente(paciente.getId());
                
                if (resultado) {
                    mensaje.setValue("Paciente eliminado exitosamente");
                    cargarPacientes();
                } else {
                    mensaje.setValue("No se pudo eliminar el paciente");
                }
            } else {
                mensaje.setValue("Paciente no encontrado");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al eliminar paciente: " + e.getMessage());
            Log.e(TAG, "Error eliminando paciente", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void setPacienteActual(Paciente paciente) {
        this.pacienteActual = paciente;
    }

    public Paciente getPacienteActual() {
        return pacienteActual;
    }

    public void limpiarPacienteActual() {
        this.pacienteActual = null;
    }

    public void limpiarMensaje() {
        mensaje.setValue("");
    }
}
