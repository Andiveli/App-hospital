package com.example.hospital.data.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class Cita implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idCita;
    private LocalTime hora;
    private DayOfWeek dia;
    private String paciente;
    private String medico;
    private EstadoCita estadoCita; 

    public Cita(int idCita, LocalTime hora, DayOfWeek dia, String paciente, String medico) {
        this.idCita = idCita;
        this.hora = hora;
        this.dia = dia;
        this.paciente = paciente;
        this.medico = medico;
        this.estadoCita = EstadoCita.PROGRAMADA;
    }

    @Override
    public String toString() {
        return "Id: " + idCita + 
               ", Día: " + dia + 
               ", Hora: " + hora + 
               ", Paciente: " + paciente + 
               ", Médico: " + medico + 
               ", Estado: " + estadoCita;
    }

    // Getters y setters
    public int getIdCita() {
        return idCita;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public DayOfWeek getDia() {
        return dia;
    }

    public void setDia(DayOfWeek dia) {
        this.dia = dia;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public EstadoCita getEstadoCita() {
        return estadoCita;
    }

    public void setEstadoCita(EstadoCita estadoCita) {
        this.estadoCita = estadoCita;
    }

    public void marcarComoPagado() {
        this.estadoCita = EstadoCita.ATENDIDA;
    }
}
