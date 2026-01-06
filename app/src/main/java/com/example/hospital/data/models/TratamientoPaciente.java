package com.example.hospital.data.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TratamientoPaciente implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private Paciente paciente;
    private Tratamiento tratamiento;
    private LocalDateTime fechaAsignacion;
    private String estado; // ACTIVO, COMPLETADO, CANCELADO
    private String observaciones;

    public TratamientoPaciente(Paciente paciente, Tratamiento tratamiento) {
        this.paciente = paciente;
        this.tratamiento = tratamiento;
        this.fechaAsignacion = LocalDateTime.now();
        this.estado = "ACTIVO";
        this.observaciones = "";
        this.id = 0;
    }

    public TratamientoPaciente(int id, Paciente paciente, Tratamiento tratamiento, 
                              LocalDateTime fechaAsignacion, String estado, String observaciones) {
        this.id = id;
        this.paciente = paciente;
        this.tratamiento = tratamiento;
        this.fechaAsignacion = fechaAsignacion;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Tratamiento getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(Tratamiento tratamiento) {
        this.tratamiento = tratamiento;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public double getCostoTotal() {
        return tratamiento.calcularCosto();
    }

    public String getFechaFormateada() {
        if (fechaAsignacion != null) {
            return fechaAsignacion.getDayOfMonth() + "/" + 
                   fechaAsignacion.getMonthValue() + "/" + 
                   fechaAsignacion.getYear() + " " +
                   String.format("%02d:%02d", fechaAsignacion.getHour(), fechaAsignacion.getMinute());
        }
        return "N/A";
    }

    @Override
    public String toString() {
        return "Tratamiento: " + tratamiento.getNombre() + 
               " | Paciente: " + paciente.getNombre() + " " + paciente.getApellido() +
               " | Fecha: " + getFechaFormateada() +
               " | Estado: " + estado;
    }

    public String getDetallesTratamiento() {
        StringBuilder detalles = new StringBuilder();
        detalles.append("Tipo: ").append(tratamiento.getTipo()).append("\n");
        
        if (tratamiento instanceof Medicacion) {
            Medicacion med = (Medicacion) tratamiento;
            detalles.append("Duración: ").append(med.getDuracion()).append(" días\n");
            detalles.append("Precio por día: $").append(String.format("%.2f", med.getPrecio()));
        } else if (tratamiento instanceof Cirugia) {
            Cirugia cir = (Cirugia) tratamiento;
            detalles.append("Duración: ").append(cir.getDuracion()).append(" horas\n");
            detalles.append("Precio por hora: $").append(String.format("%.2f", cir.getPrecio()));
        } else if (tratamiento instanceof Terapia) {
            Terapia ter = (Terapia) tratamiento;
            detalles.append("Sesiones: ").append(ter.getDuracion()).append("\n");
            detalles.append("Precio por sesión: $").append(String.format("%.2f", ter.getPrecio()));
        }
        
        detalles.append("\nCosto total: $").append(String.format("%.2f", getCostoTotal()));
        
        return detalles.toString();
    }
}