package com.example.hospital.data.repository;

import android.content.Context;

import com.example.hospital.data.models.Cita;
import com.example.hospital.data.models.EstadoCita;
import com.example.hospital.data.storage.FileStorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CitaRepository {
    private static final String CITAS_FILE = "citas.dat";
    private final FileStorageManager storageManager;
    private List<Cita> citasCache;

    public CitaRepository(Context context) {
        this.storageManager = new FileStorageManager(context);
        this.citasCache = new ArrayList<>();
        cargarCitas();
    }

    public List<Cita> getAllCitas() {
        return new ArrayList<>(citasCache);
    }

    public List<Cita> getCitasPorEstado(EstadoCita estado) {
        List<Cita> resultado = new ArrayList<>();
        for (Cita cita : citasCache) {
            if (cita.getEstadoCita() == estado) {
                resultado.add(cita);
            }
        }
        return resultado;
    }

    public List<Cita> getCitasPorPaciente(String correoPaciente) {
        List<Cita> resultado = new ArrayList<>();
        for (Cita cita : citasCache) {
            if (cita.getPaciente().equalsIgnoreCase(correoPaciente)) {
                resultado.add(cita);
            }
        }
        return resultado;
    }

    public List<Cita> getCitasPorMedico(String correoMedico) {
        List<Cita> resultado = new ArrayList<>();
        for (Cita cita : citasCache) {
            if (cita.getMedico().equalsIgnoreCase(correoMedico)) {
                resultado.add(cita);
            }
        }
        return resultado;
    }

    public List<Cita> getCitasPorDia(String dia) {
        List<Cita> resultado = new ArrayList<>();
        for (Cita cita : citasCache) {
            if (cita.getDia().name().equalsIgnoreCase(dia)) {
                resultado.add(cita);
            }
        }
        return resultado;
    }

    public Optional<Cita> getCitaPorId(int id) {
        return citasCache.stream()
                .filter(c -> c.getIdCita() == id)
                .findFirst();
    }

    public boolean guardarCita(Cita cita) {
        // Asignar ID automático
        int nuevoId = citasCache.size() + 1;
        Cita nuevaCita = new Cita(
                nuevoId,
                cita.getHora(),
                cita.getDia(),
                cita.getPaciente(),
                cita.getMedico()
        );
        nuevaCita.setEstadoCita(cita.getEstadoCita());

        citasCache.add(nuevaCita);
        return guardarCambios();
    }

    public boolean actualizarCita(Cita cita) {
        Optional<Cita> existente = getCitaPorId(cita.getIdCita());
        if (!existente.isPresent()) {
            return false;
        }

        int index = citasCache.indexOf(existente.get());
        if (index != -1) {
            citasCache.set(index, cita);
            return guardarCambios();
        }
        return false;
    }

    public boolean cancelarCita(int id) {
        Optional<Cita> cita = getCitaPorId(id);
        if (cita.isPresent()) {
            Cita citaExistente = cita.get();
            citaExistente.setEstadoCita(EstadoCita.CANCELADA);
            return guardarCambios();
        }
        return false;
    }

    public boolean marcarComoAtendida(int id) {
        Optional<Cita> cita = getCitaPorId(id);
        if (cita.isPresent()) {
            Cita citaExistente = cita.get();
            citaExistente.setEstadoCita(EstadoCita.ATENDIDA);
            return guardarCambios();
        }
        return false;
    }

    public boolean eliminarCita(int id) {
        Optional<Cita> cita = getCitaPorId(id);
        if (cita.isPresent()) {
            citasCache.remove(cita.get());
            return guardarCambios();
        }
        return false;
    }

    public boolean citaExiste(int id) {
        return getCitaPorId(id).isPresent();
    }

    private void cargarCitas() {
        try {
            citasCache = storageManager.loadList(CITAS_FILE);
        } catch (Exception e) {
            citasCache = new ArrayList<>();
        }
    }

    private boolean guardarCambios() {
        try {
            storageManager.saveList(CITAS_FILE, citasCache);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Métodos útiles para filtros combinados
    public List<Cita> getCitasActivasPorMedico(String correoMedico) {
        List<Cita> resultado = new ArrayList<>();
        for (Cita cita : citasCache) {
            if (cita.getMedico().equalsIgnoreCase(correoMedico) && 
                (cita.getEstadoCita() == EstadoCita.PROGRAMADA || 
                 cita.getEstadoCita() == EstadoCita.ATENDIDA)) {
                resultado.add(cita);
            }
        }
        return resultado;
    }

    public List<Cita> getCitasProgramadas() {
        return getCitasPorEstado(EstadoCita.PROGRAMADA);
    }

    public List<Cita> getCitasCanceladas() {
        return getCitasPorEstado(EstadoCita.CANCELADA);
    }

    public List<Cita> getCitasAtendidas() {
        return getCitasPorEstado(EstadoCita.ATENDIDA);
    }
}