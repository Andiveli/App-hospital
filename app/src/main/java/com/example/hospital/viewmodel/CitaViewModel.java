package com.example.hospital.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hospital.data.models.Cita;
import com.example.hospital.data.models.EstadoCita;
import com.example.hospital.data.models.Medico;
import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.repository.CitaRepository;
import com.example.hospital.data.repository.MedicoRepository;
import com.example.hospital.data.repository.PacienteRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class CitaViewModel extends AndroidViewModel {
    private static final String TAG = "CitaViewModel";
    
    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    
    private final MutableLiveData<List<Cita>> citas;
    private final MutableLiveData<String> mensaje;
    private final MutableLiveData<Boolean> loading;
    private Cita citaActual;

    public CitaViewModel(Application application) {
        super(application);
        this.citaRepository = new CitaRepository(application.getApplicationContext());
        this.medicoRepository = new MedicoRepository(application.getApplicationContext());
        this.pacienteRepository = new PacienteRepository(application.getApplicationContext());
        
        this.citas = new MutableLiveData<>();
        this.mensaje = new MutableLiveData<>();
        this.loading = new MutableLiveData<>();
        
        cargarCitas();
    }

    public LiveData<List<Cita>> getCitas() {
        return citas;
    }
    
    public LiveData<String> getMensaje() {
        return mensaje;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void cargarCitas() {
        loading.setValue(true);
        try {
            List<Cita> lista = citaRepository.getAllCitas();
            citas.setValue(lista);
            mensaje.setValue("Citas cargadas: " + lista.size());
            Log.d(TAG, "Cargadas " + lista.size() + " citas");
        } catch (Exception e) {
            mensaje.setValue("Error al cargar citas: " + e.getMessage());
            Log.e(TAG, "Error cargando citas", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void guardarCita(String horaStr, String diaStr, String correoPaciente, String correoMedico) {
        guardarCita(horaStr, diaStr, correoPaciente, correoMedico, EstadoCita.PROGRAMADA, false);
    }

    public void guardarCita(String horaStr, String diaStr, String correoPaciente, String correoMedico, 
                           EstadoCita estado, boolean esEdicion) {
        loading.setValue(true);
        
        // Validaciones básicas
        if (horaStr == null || horaStr.trim().isEmpty()) {
            mensaje.setValue("Error: La hora es obligatoria");
            loading.setValue(false);
            return;
        }
        
        if (diaStr == null || diaStr.trim().isEmpty()) {
            mensaje.setValue("Error: El día es obligatorio");
            loading.setValue(false);
            return;
        }
        
        if (correoPaciente == null || !correoPaciente.contains("@")) {
            mensaje.setValue("Error: El correo del paciente no es válido");
            loading.setValue(false);
            return;
        }
        
        if (correoMedico == null || !correoMedico.contains("@")) {
            mensaje.setValue("Error: El correo del médico no es válido");
            loading.setValue(false);
            return;
        }
        
        try {
            // Parsear hora
            LocalTime hora = LocalTime.parse(horaStr);
            DayOfWeek dia = DayOfWeek.valueOf(diaStr.toUpperCase());
            
            // Verificar que paciente y médico existan
            Optional<Paciente> paciente = pacienteRepository.getPacientePorCorreo(correoPaciente.trim());
            if (!paciente.isPresent()) {
                mensaje.setValue("Error: El paciente no está registrado");
                loading.setValue(false);
                return;
            }
            
            Optional<Medico> medico = medicoRepository.getMedicoPorCorreo(correoMedico.trim());
            if (!medico.isPresent()) {
                mensaje.setValue("Error: El médico no está registrado");
                loading.setValue(false);
                return;
            }
            
            // Verificar que el médico esté disponible
            Medico medicoObj = medico.get();
            if (!medicoObj.isDisponible(hora, dia)) {
                mensaje.setValue("Error: El médico no está disponible en ese horario");
                loading.setValue(false);
                return;
            }
            
            // Verificar que no haya otra cita en el mismo horario
            List<Cita> citasExistentes = citaRepository.getCitasPorMedico(correoMedico);
            for (Cita existente : citasExistentes) {
                if (existente.getDia() == dia && 
                    existente.getHora().equals(hora) && 
                    existente.getEstadoCita() != EstadoCita.CANCELADA &&
                    (esEdicion == false || existente.getIdCita() != citaActual.getIdCita())) {
                    mensaje.setValue("Error: Ya existe una cita programada para ese médico en ese horario");
                    loading.setValue(false);
                    return;
                }
            }
            
            // Guardar o actualizar cita
            if (esEdicion && citaActual != null) {
                citaActual.setHora(hora);
                citaActual.setDia(dia);
                citaActual.setPaciente(correoPaciente.trim());
                citaActual.setMedico(correoMedico.trim());
                citaActual.setEstadoCita(estado);
                
                boolean resultado = citaRepository.actualizarCita(citaActual);
                
                if (resultado) {
                    mensaje.setValue("Cita actualizada exitosamente");
                    cargarCitas();
                } else {
                    mensaje.setValue("No se pudo actualizar la cita");
                }
            } else {
                Cita nuevaCita = new Cita(0, hora, dia, correoPaciente.trim(), correoMedico.trim());
                nuevaCita.setEstadoCita(estado);
                
                boolean resultado = citaRepository.guardarCita(nuevaCita);
                
                if (resultado) {
                    mensaje.setValue("Cita guardada exitosamente");
                    cargarCitas();
                } else {
                    mensaje.setValue("No se pudo guardar la cita");
                }
            }
            
        } catch (DateTimeParseException e) {
            mensaje.setValue("Error: Formato de hora inválido. Use HH:mm");
        } catch (IllegalArgumentException e) {
            mensaje.setValue("Error: Día inválido. Use: LUNES, MARTES, etc.");
        } catch (Exception e) {
            mensaje.setValue("Error al guardar cita: " + e.getMessage());
            Log.e(TAG, "Error guardando cita", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorEstado(EstadoCita estado) {
        loading.setValue(true);
        try {
            List<Cita> filtradas = citaRepository.getCitasPorEstado(estado);
            citas.setValue(filtradas);
            mensaje.setValue("Citas con estado " + estado.name() + ": " + filtradas.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por estado: " + e.getMessage());
            Log.e(TAG, "Error filtrando por estado", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorPaciente(String correoPaciente) {
        loading.setValue(true);
        try {
            List<Cita> filtradas = citaRepository.getCitasPorPaciente(correoPaciente);
            citas.setValue(filtradas);
            mensaje.setValue("Citas del paciente: " + filtradas.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por paciente: " + e.getMessage());
            Log.e(TAG, "Error filtrando por paciente", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorMedico(String correoMedico) {
        loading.setValue(true);
        try {
            List<Cita> filtradas = citaRepository.getCitasPorMedico(correoMedico);
            citas.setValue(filtradas);
            mensaje.setValue("Citas del médico: " + filtradas.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por médico: " + e.getMessage());
            Log.e(TAG, "Error filtrando por médico", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorDia(String dia) {
        loading.setValue(true);
        try {
            List<Cita> filtradas = citaRepository.getCitasPorDia(dia);
            citas.setValue(filtradas);
            mensaje.setValue("Citas del día " + dia + ": " + filtradas.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por día: " + e.getMessage());
            Log.e(TAG, "Error filtrando por día", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void cancelarCita(int id) {
        loading.setValue(true);
        try {
            boolean resultado = citaRepository.cancelarCita(id);
            
            if (resultado) {
                mensaje.setValue("Cita cancelada exitosamente");
                cargarCitas();
            } else {
                mensaje.setValue("No se pudo cancelar la cita");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al cancelar cita: " + e.getMessage());
            Log.e(TAG, "Error cancelando cita", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void marcarComoAtendida(int id) {
        loading.setValue(true);
        try {
            boolean resultado = citaRepository.marcarComoAtendida(id);
            
            if (resultado) {
                mensaje.setValue("Cita marcada como atendida");
                cargarCitas();
            } else {
                mensaje.setValue("No se pudo marcar la cita como atendida");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al marcar cita como atendida: " + e.getMessage());
            Log.e(TAG, "Error marcando cita como atendida", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void eliminarCita(int id) {
        loading.setValue(true);
        try {
            boolean resultado = citaRepository.eliminarCita(id);
            
            if (resultado) {
                mensaje.setValue("Cita eliminada exitosamente");
                cargarCitas();
            } else {
                mensaje.setValue("No se pudo eliminar la cita");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al eliminar cita: " + e.getMessage());
            Log.e(TAG, "Error eliminando cita", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void setCitaActual(Cita cita) {
        this.citaActual = cita;
    }

    public Cita getCitaActual() {
        return citaActual;
    }

    public void limpiarCitaActual() {
        this.citaActual = null;
    }

    public void limpiarMensaje() {
        mensaje.setValue("");
    }

    // Métodos útiles para filtros rápidos
    public void cargarCitasProgramadas() {
        filtrarPorEstado(EstadoCita.PROGRAMADA);
    }

    public void cargarCitasAtendidas() {
        filtrarPorEstado(EstadoCita.ATENDIDA);
    }

    public void cargarCitasCanceladas() {
        filtrarPorEstado(EstadoCita.CANCELADA);
    }
}