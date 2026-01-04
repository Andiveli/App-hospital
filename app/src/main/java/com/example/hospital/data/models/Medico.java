package com.example.hospital.data.models;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.DayOfWeek;

public class Medico extends Persona implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private HorarioAtencion horarioAtencion;
    private String genero;
    private String especialidad;
    private boolean activo;

    public Medico(int id, String nombre, String apellido, String correo, String cedula, 
                  HorarioAtencion horarioAtencion, String genero, String especialidad, boolean activo) {
        super(id, nombre, apellido, correo, cedula);
        this.horarioAtencion = horarioAtencion;
        this.genero = genero;
        this.especialidad = especialidad;
        this.activo = activo;
    }

    public boolean isDisponible(LocalTime hora, DayOfWeek dia) {
        boolean disponible = horarioAtencion.isDisponible(hora, dia);
        return disponible && activo;
    }

    @Override
    public String toString() {
        return "Dr. " + getNombre() + " " + getApellido() + 
               ", Especialidad: " + especialidad + 
               ", Correo: " + getCorreo() + 
               ", Activo: " + (activo ? "Sí" : "No");
    }

    // Getters y setters
    public HorarioAtencion getHorarioAtencion() {
        return horarioAtencion;
    }

    public void setHorarioAtencion(HorarioAtencion horarioAtencion) {
        this.horarioAtencion = horarioAtencion;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void registrarTurno() {
        System.out.println("Turno registrado para el médico: " + getNombre());
    }
}
