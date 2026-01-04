package com.example.hospital.data.repository;

import android.content.Context;

import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.storage.FileStorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PacienteRepository {
    private static final String PACIENTES_FILE = "pacientes.dat";
    private final FileStorageManager storageManager;
    private List<Paciente> pacientesCache;

    public PacienteRepository(Context context) {
        this.storageManager = new FileStorageManager(context);
        this.pacientesCache = new ArrayList<>();
        cargarPacientes();
    }

    public List<Paciente> getAllPacientes() {
        return new ArrayList<>(pacientesCache);
    }

    public Optional<Paciente> getPacientePorCorreo(String correo) {
        return pacientesCache.stream()
                .filter(p -> p.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    public Optional<Paciente> getPacientePorId(int id) {
        return pacientesCache.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    public Optional<Paciente> getPacientePorCedula(String cedula) {
        return pacientesCache.stream()
                .filter(p -> p.getCedulaString().equals(cedula))
                .findFirst();
    }

    public List<Paciente> getPacientesPorTipoSeguro(com.example.hospital.data.models.TipoSeguro tipoSeguro) {
        List<Paciente> resultado = new ArrayList<>();
        for (Paciente paciente : pacientesCache) {
            if (paciente.getTipoSeguro() == tipoSeguro) {
                resultado.add(paciente);
            }
        }
        return resultado;
    }

    public List<Paciente> buscarPacientes(String campo, String termino) {
        String terminoLower = termino.toLowerCase();
        
        return pacientesCache.stream()
                .filter(p -> {
                    switch (campo.toLowerCase()) {
                        case "nombre":
                            return p.getNombre().toLowerCase().contains(terminoLower);
                        case "apellido":
                            return p.getApellido().toLowerCase().contains(terminoLower);
                        case "cedula":
                            return p.getCedulaString().contains(terminoLower);
                        case "correo":
                            return p.getCorreo().toLowerCase().contains(terminoLower);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public boolean guardarPaciente(Paciente paciente) {
        // Verificar si ya existe
        if (getPacientePorCorreo(paciente.getCorreo()).isPresent()) {
            return false;
        }

        // Asignar ID
        int nuevoId = pacientesCache.size() + 1;
        // Creamos uno nuevo con el ID asignado
        Paciente nuevoPaciente = new Paciente(
                nuevoId,
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getCorreo(),
                paciente.getCedulaString(),
                paciente.getTipoSeguro()
        );

        pacientesCache.add(nuevoPaciente);
        return guardarCambios();
    }

    public boolean actualizarPaciente(Paciente paciente) {
        Optional<Paciente> existente = getPacientePorId(paciente.getId());
        if (!existente.isPresent()) {
            return false;
        }

        int index = pacientesCache.indexOf(existente.get());
        if (index != -1) {
            pacientesCache.set(index, paciente);
            return guardarCambios();
        }
        return false;
    }

    public boolean eliminarPaciente(int id) {
        Optional<Paciente> paciente = getPacientePorId(id);
        if (paciente.isPresent()) {
            pacientesCache.remove(paciente.get());
            return guardarCambios();
        }
        return false;
    }

    private void cargarPacientes() {
        try {
            pacientesCache = storageManager.loadList(PACIENTES_FILE);
        } catch (Exception e) {
            pacientesCache = new ArrayList<>();
        }
    }

    private boolean guardarCambios() {
        try {
            storageManager.saveList(PACIENTES_FILE, pacientesCache);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean pacienteExiste(String correo) {
        return getPacientePorCorreo(correo).isPresent();
    }
}
