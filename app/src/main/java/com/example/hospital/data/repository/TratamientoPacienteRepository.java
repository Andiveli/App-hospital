package com.example.hospital.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.hospital.data.models.Paciente;
import com.example.hospital.data.models.Tratamiento;
import com.example.hospital.data.models.TratamientoPaciente;
import com.example.hospital.data.storage.FileStorageManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TratamientoPacienteRepository {
    private static final String FILE_NAME = "tratamientos_paciente.txt";
    private final Context context;
    private final FileStorageManager storageManager;
    private int nextId = 1;

    public TratamientoPacienteRepository(Context context) {
        this.context = context;
        this.storageManager = new FileStorageManager(context);
        loadNextId();
    }

    private void loadNextId() {
        List<TratamientoPaciente> tratamientos = cargarTodos();
        for (TratamientoPaciente tp : tratamientos) {
            if (tp.getId() >= nextId) {
                nextId = tp.getId() + 1;
            }
        }
    }

    public List<TratamientoPaciente> cargarTodos() {
        List<TratamientoPaciente> tratamientos = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);

        if (!file.exists()) {
            return tratamientos;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TratamientoPaciente tratamiento = parseTratamientoPaciente(line);
                if (tratamiento != null) {
                    tratamientos.add(tratamiento);
                }
            }
        } catch (IOException e) {
            Log.e("TratamientoPacienteRepo", "Error al cargar tratamientos", e);
        }

        return tratamientos;
    }

    public List<TratamientoPaciente> cargarPorPaciente(String correoPaciente) {
        return cargarTodos().stream()
                .filter(tp -> tp.getPaciente().getCorreo().equals(correoPaciente))
                .collect(Collectors.toList());
    }

    public List<TratamientoPaciente> cargarPorEstado(String estado) {
        return cargarTodos().stream()
                .filter(tp -> tp.getEstado().equals(estado))
                .collect(Collectors.toList());
    }

    public TratamientoPaciente guardar(TratamientoPaciente tratamiento) {
        if (tratamiento.getId() == 0) {
            tratamiento.setId(nextId++);
        }

        List<TratamientoPaciente> tratamientos = cargarTodos();
        
        // Buscar si ya existe uno con el mismo ID y reemplazarlo
        for (int i = 0; i < tratamientos.size(); i++) {
            if (tratamientos.get(i).getId() == tratamiento.getId()) {
                tratamientos.set(i, tratamiento);
                guardarTodos(tratamientos);
                return tratamiento;
            }
        }

        // Si no existe, agregarlo al final
        tratamientos.add(tratamiento);
        guardarTodos(tratamientos);
        return tratamiento;
    }

    public boolean eliminar(int id) {
        List<TratamientoPaciente> tratamientos = cargarTodos();
        boolean eliminado = tratamientos.removeIf(tp -> tp.getId() == id);
        
        if (eliminado) {
            guardarTodos(tratamientos);
        }
        
        return eliminado;
    }

    public TratamientoPaciente buscarPorId(int id) {
        return cargarTodos().stream()
                .filter(tp -> tp.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void guardarTodos(List<TratamientoPaciente> tratamientos) {
        File file = new File(context.getFilesDir(), FILE_NAME);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (TratamientoPaciente tratamiento : tratamientos) {
                writer.write(formatTratamientoPaciente(tratamiento));
                writer.newLine();
            }
        } catch (IOException e) {
            Log.e("TratamientoPacienteRepo", "Error al guardar tratamientos", e);
        }
    }

    private String formatTratamientoPaciente(TratamientoPaciente tp) {
        String pacienteData = formatPaciente(tp.getPaciente());
        String tratamientoData = formatTratamiento(tp.getTratamiento());
        String fecha = tp.getFechaAsignacion() != null ? 
                      tp.getFechaAsignacion().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
        
        return tp.getId() + "|" + pacienteData + "|" + tratamientoData + "|" + 
               fecha + "|" + tp.getEstado() + "|" + tp.getObservaciones();
    }

    private String formatPaciente(Paciente paciente) {
        return paciente.getId() + ";" + paciente.getNombre() + ";" + 
               paciente.getApellido() + ";" + paciente.getCorreo() + ";" + 
               paciente.getCedulaString() + ";" + paciente.getTipoSeguro();
    }

    private String formatTratamiento(Tratamiento tratamiento) {
        String tipo = tratamiento.getClass().getSimpleName();
        return tipo + ";" + tratamiento.getNombre() + ";" + 
               tratamiento.getDuracion() + ";" + tratamiento.getPrecio();
    }

    private TratamientoPaciente parseTratamientoPaciente(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length != 6) return null;

            int id = Integer.parseInt(parts[0]);
            Paciente paciente = parsePaciente(parts[1]);
            Tratamiento tratamiento = parseTratamiento(parts[2]);
            
            if (paciente == null || tratamiento == null) return null;

            LocalDateTime fecha = parts[3].isEmpty() ? null : 
                                 LocalDateTime.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String estado = parts[4];
            String observaciones = parts[5];

            return new TratamientoPaciente(id, paciente, tratamiento, fecha, estado, observaciones);

        } catch (Exception e) {
            Log.e("TratamientoPacienteRepo", "Error parseando línea: " + line, e);
            return null;
        }
    }

    private Paciente parsePaciente(String pacienteData) {
        try {
            String[] parts = pacienteData.split(";");
            if (parts.length != 6) return null;

            int id = Integer.parseInt(parts[0]);
            String nombre = parts[1];
            String apellido = parts[2];
            String correo = parts[3];
            String cedula = parts[4];
            String tipoSeguroStr = parts[5];

            // Crear paciente temporal (no necesitamos el completo)
            return new Paciente(id, nombre, apellido, correo, cedula, 
                              com.example.hospital.data.models.TipoSeguro.valueOf(tipoSeguroStr));

        } catch (Exception e) {
            Log.e("TratamientoPacienteRepo", "Error parseando paciente: " + pacienteData, e);
            return null;
        }
    }

    private Tratamiento parseTratamiento(String tratamientoData) {
        try {
            String[] parts = tratamientoData.split(";");
            if (parts.length != 4) return null;

            String tipo = parts[0];
            String nombre = parts[1];
            int duracion = Integer.parseInt(parts[2]);
            double precio = Double.parseDouble(parts[3]);

            switch (tipo) {
                case "Medicacion":
                    return new com.example.hospital.data.models.Medicacion(nombre, duracion, precio);
                case "Cirugia":
                    return new com.example.hospital.data.models.Cirugia(nombre, duracion, precio);
                case "Terapia":
                    return new com.example.hospital.data.models.Terapia(nombre, duracion, precio);
                default:
                    return null;
            }

        } catch (Exception e) {
            Log.e("TratamientoPacienteRepo", "Error parseando tratamiento: " + tratamientoData, e);
            return null;
        }
    }

    public List<String> getPacientesConTratamientos() {
        return cargarTodos().stream()
                .map(tp -> tp.getPaciente().getNombre() + " " + tp.getPaciente().getApellido() + 
                           " (" + tp.getPaciente().getCorreo() + ")")
                .distinct()
                .collect(Collectors.toList());
    }
}