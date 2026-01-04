package com.example.hospital.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hospital.data.models.HorarioAtencion;
import com.example.hospital.data.models.Medico;
import com.example.hospital.data.repository.MedicoRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class MedicoViewModel extends AndroidViewModel {
    private static final String TAG = "MedicoViewModel";
    
    private final MedicoRepository medicoRepository;
    private final MutableLiveData<List<Medico>> medicos;
    private final MutableLiveData<String> mensaje;
    private final MutableLiveData<Boolean> loading;
    private Medico medicoActual;

    public MedicoViewModel(Application application) {
        super(application);
        this.medicoRepository = new MedicoRepository(application.getApplicationContext());
        this.medicos = new MutableLiveData<>();
        this.mensaje = new MutableLiveData<>();
        this.loading = new MutableLiveData<>();
        
        cargarMedicos();
    }

    public LiveData<List<Medico>> getMedicos() {
        return medicos;
    }
    
    public LiveData<String> getMensaje() {
        return mensaje;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void cargarMedicos() {
        loading.setValue(true);
        try {
            List<Medico> lista = medicoRepository.getAllMedicos();
            medicos.setValue(lista);
            mensaje.setValue("Médicos cargados: " + lista.size());
            Log.d(TAG, "Cargados " + lista.size() + " médicos");
        } catch (Exception e) {
            mensaje.setValue("Error al cargar médicos: " + e.getMessage());
            Log.e(TAG, "Error cargando médicos", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void guardarMedico(String nombre, String apellido, String correo, 
                           String cedula, String genero, String especialidad,
                           HorarioAtencion horarioAtencion, boolean activo) {
        guardarMedico(nombre, apellido, correo, cedula, genero, especialidad, 
                     horarioAtencion, activo, false);
    }

    public void guardarMedico(String nombre, String apellido, String correo, 
                           String cedula, String genero, String especialidad,
                           HorarioAtencion horarioAtencion, boolean activo, boolean esEdicion) {
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
        
        if (genero == null || genero.trim().isEmpty()) {
            mensaje.setValue("Error: El género es obligatorio");
            loading.setValue(false);
            return;
        }
        
        if (especialidad == null || especialidad.trim().isEmpty()) {
            mensaje.setValue("Error: La especialidad es obligatoria");
            loading.setValue(false);
            return;
        }
        
        if (horarioAtencion == null) {
            mensaje.setValue("Error: El horario de atención es obligatorio");
            loading.setValue(false);
            return;
        }
        
        try {
            if (esEdicion && medicoActual != null) {
                medicoActual.setNombre(nombre.trim());
                medicoActual.setApellido(apellido.trim());
                medicoActual.setCorreo(correo.trim().toLowerCase());
                medicoActual.setGenero(genero.trim());
                medicoActual.setEspecialidad(especialidad.trim());
                medicoActual.setHorarioAtencion(horarioAtencion);
                medicoActual.setActivo(activo);
                
                boolean resultado = medicoRepository.actualizarMedico(medicoActual);
                
                if (resultado) {
                    mensaje.setValue("Médico actualizado exitosamente");
                    cargarMedicos();
                } else {
                    mensaje.setValue("No se pudo actualizar el médico");
                }
            } else {
                Medico nuevoMedico = new Medico(
                    0, nombre.trim(), apellido.trim(), 
                    correo.trim().toLowerCase(), cedula.trim(), 
                    horarioAtencion, genero.trim(), especialidad.trim(), activo
                );
                
                boolean resultado = medicoRepository.guardarMedico(nuevoMedico);
                
                if (resultado) {
                    mensaje.setValue("Médico guardado exitosamente");
                    cargarMedicos();
                } else {
                    mensaje.setValue("El médico ya está registrado");
                }
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al guardar médico: " + e.getMessage());
            Log.e(TAG, "Error guardando médico", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorEspecialidad(String especialidad) {
        loading.setValue(true);
        try {
            List<Medico> filtrados = medicoRepository.getMedicosPorEspecialidad(especialidad);
            medicos.setValue(filtrados);
            mensaje.setValue("Médicos con especialidad " + especialidad + ": " + filtrados.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por especialidad: " + e.getMessage());
            Log.e(TAG, "Error filtrando por especialidad", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorGenero(String genero) {
        loading.setValue(true);
        try {
            List<Medico> filtrados = medicoRepository.getMedicosPorGenero(genero);
            medicos.setValue(filtrados);
            mensaje.setValue("Médicos de género " + genero + ": " + filtrados.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por género: " + e.getMessage());
            Log.e(TAG, "Error filtrando por género", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            cargarMedicos();
            return;
        }
        
        loading.setValue(true);
        try {
            Optional<Medico> encontrado = medicoRepository.getMedicoPorCorreo(correo.trim());
            if (encontrado.isPresent()) {
                medicos.setValue(List.of(encontrado.get()));
                mensaje.setValue("Médico encontrado: " + encontrado.get().getNombre());
            } else {
                mensaje.setValue("Médico no encontrado");
                medicos.setValue(List.of());
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al buscar médico: " + e.getMessage());
            Log.e(TAG, "Error buscando médico", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void eliminarMedico(int id) {
        loading.setValue(true);
        try {
            boolean resultado = medicoRepository.eliminarMedico(id);
            
            if (resultado) {
                mensaje.setValue("Médico eliminado exitosamente");
                cargarMedicos();
            } else {
                mensaje.setValue("No se pudo eliminar el médico");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al eliminar médico: " + e.getMessage());
            Log.e(TAG, "Error eliminando médico", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void activarDesactivarMedico(int id) {
        loading.setValue(true);
        try {
            Optional<Medico> medicoOpt = medicoRepository.getMedicoPorId(id);
            if (medicoOpt.isPresent()) {
                Medico medico = medicoOpt.get();
                medico.setActivo(!medico.isActivo());
                
                boolean resultado = medicoRepository.actualizarMedico(medico);
                
                if (resultado) {
                    String estado = medico.isActivo() ? "activado" : "desactivado";
                    mensaje.setValue("Médico " + estado + " exitosamente");
                    cargarMedicos();
                } else {
                    mensaje.setValue("No se pudo actualizar el estado del médico");
                }
            } else {
                mensaje.setValue("Médico no encontrado");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al cambiar estado: " + e.getMessage());
            Log.e(TAG, "Error cambiando estado médico", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void setMedicoActual(Medico medico) {
        this.medicoActual = medico;
    }

    public Medico getMedicoActual() {
        return medicoActual;
    }

    public void limpiarMedicoActual() {
        this.medicoActual = null;
    }

    public void limpiarMensaje() {
        mensaje.setValue("");
    }

    public void cargarMedicosActivos() {
        loading.setValue(true);
        try {
            List<Medico> activos = medicoRepository.getMedicosActivos();
            medicos.setValue(activos);
            mensaje.setValue("Médicos activos: " + activos.size());
        } catch (Exception e) {
            mensaje.setValue("Error al cargar médicos activos: " + e.getMessage());
            Log.e(TAG, "Error cargando médicos activos", e);
        } finally {
            loading.setValue(false);
        }
    }
}