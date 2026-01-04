package com.example.hospital.data.repository;

import android.content.Context;

import com.example.hospital.data.models.Medico;
import com.example.hospital.data.storage.FileStorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicoRepository {
    private static final String MEDICOS_FILE = "medicos.dat";
    private final FileStorageManager storageManager;
    private List<Medico> medicosCache;

    public MedicoRepository(Context context) {
        this.storageManager = new FileStorageManager(context);
        this.medicosCache = new ArrayList<>();
        cargarMedicos();
    }

    public List<Medico> getAllMedicos() {
        return new ArrayList<>(medicosCache);
    }

    public List<Medico> getMedicosActivos() {
        List<Medico> activos = new ArrayList<>();
        for (Medico medico : medicosCache) {
            if (medico.isActivo()) {
                activos.add(medico);
            }
        }
        return activos;
    }

    public List<Medico> getMedicosPorEspecialidad(String especialidad) {
        List<Medico> resultado = new ArrayList<>();
        for (Medico medico : medicosCache) {
            if (medico.getEspecialidad().equalsIgnoreCase(especialidad)) {
                resultado.add(medico);
            }
        }
        return resultado;
    }

    public List<Medico> getMedicosPorGenero(String genero) {
        List<Medico> resultado = new ArrayList<>();
        for (Medico medico : medicosCache) {
            if (medico.getGenero().equalsIgnoreCase(genero)) {
                resultado.add(medico);
            }
        }
        return resultado;
    }

    public Optional<Medico> getMedicoPorCorreo(String correo) {
        return medicosCache.stream()
                .filter(m -> m.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    public Optional<Medico> getMedicoPorId(int id) {
        return medicosCache.stream()
                .filter(m -> m.getId() == id)
                .findFirst();
    }

    public boolean guardarMedico(Medico medico) {
        // Verificar si ya existe
        if (getMedicoPorCorreo(medico.getCorreo()).isPresent()) {
            return false;
        }

        // Asignar ID
        int nuevoId = medicosCache.size() + 1;
        Medico nuevoMedico = new Medico(
                nuevoId,
                medico.getNombre(),
                medico.getApellido(),
                medico.getCorreo(),
                medico.getCedulaString(),
                medico.getHorarioAtencion(),
                medico.getGenero(),
                medico.getEspecialidad(),
                medico.isActivo()
        );

        medicosCache.add(nuevoMedico);
        return guardarCambios();
    }

    public boolean actualizarMedico(Medico medico) {
        Optional<Medico> existente = getMedicoPorId(medico.getId());
        if (!existente.isPresent()) {
            return false;
        }

        int index = medicosCache.indexOf(existente.get());
        if (index != -1) {
            medicosCache.set(index, medico);
            return guardarCambios();
        }
        return false;
    }

    public boolean eliminarMedico(int id) {
        Optional<Medico> medico = getMedicoPorId(id);
        if (medico.isPresent()) {
            medicosCache.remove(medico.get());
            return guardarCambios();
        }
        return false;
    }

    private void cargarMedicos() {
        try {
            medicosCache = storageManager.loadList(MEDICOS_FILE);
        } catch (Exception e) {
            medicosCache = new ArrayList<>();
        }
    }

    private boolean guardarCambios() {
        try {
            storageManager.saveList(MEDICOS_FILE, medicosCache);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean medicoExiste(String correo) {
        return getMedicoPorCorreo(correo).isPresent();
    }
}