package com.example.hospital.data.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HorarioAtencion implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EnumSet<DayOfWeek> dias;
    private int duracionCita;
    private Map<DayOfWeek, Set<LocalTime>> horasOcupadasPorDia;
    
    public HorarioAtencion(LocalTime horaInicio, LocalTime horaFin, EnumSet<DayOfWeek> dias) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.dias = dias;
        this.duracionCita = 60;
        this.horasOcupadasPorDia = new HashMap<>();
        for (DayOfWeek dia : dias) {
            horasOcupadasPorDia.put(dia, new HashSet<>());
        }
    }

    public boolean isDisponible(LocalTime hora, DayOfWeek dia) {
        if (!dias.contains(dia)) return false;
        if (hora.isBefore(horaInicio) || hora.plusMinutes(duracionCita).isAfter(horaFin)) return false;

        Set<LocalTime> ocupadas = horasOcupadasPorDia.get(dia);
        return !ocupadas.contains(hora);
    }

    public boolean registrarCita(DayOfWeek dia, LocalTime hora) {
        if (!dias.contains(dia)) return false;
        if (hora.isBefore(horaInicio) || hora.plusMinutes(duracionCita).isAfter(horaFin)) return false;

        Set<LocalTime> ocupadas = horasOcupadasPorDia.get(dia);
        if (ocupadas.contains(hora)) return false;

        ocupadas.add(hora);
        return true;
    }

    public boolean modificarCita(LocalTime nuevaHora, DayOfWeek nuevoDia, LocalTime horaAnterior, DayOfWeek diaAnterior) {
        if(cancelarCita(diaAnterior, horaAnterior)) {
            return registrarCita(nuevoDia, nuevaHora);
        }
        return false;
    }

    public boolean cancelarCita(DayOfWeek dia, LocalTime hora) {
        if (!dias.contains(dia)) return false;

        Set<LocalTime> ocupadas = horasOcupadasPorDia.get(dia);
        return ocupadas.remove(hora);
    }

    // Getters y setters
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public EnumSet<DayOfWeek> getDias() { return dias; }
    public int getDuracionCita() { return duracionCita; }
    public Map<DayOfWeek, Set<LocalTime>> getHorasOcupadasPorDia() { return horasOcupadasPorDia; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        sb.append("📅 Horario del médico:\n");
        for (DayOfWeek dia : dias) {
            sb.append(dia).append(":\n");
            LocalTime actual = horaInicio;
            while (!actual.isAfter(horaFin.minusMinutes(duracionCita))) {
                boolean ocupada = horasOcupadasPorDia.get(dia).contains(actual);
                sb.append("  ").append(actual.format(formatter))
                  .append(" - ").append(ocupada ? "❌ No disponible" : "Disponible").append("\n");
                actual = actual.plusMinutes(duracionCita);
            }
        }
        return sb.toString();
    }
}