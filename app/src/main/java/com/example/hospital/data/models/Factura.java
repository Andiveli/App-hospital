package com.example.hospital.data.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Factura implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int id; 
    private final String paciente;
    private final String tratamientos;
    private final double total;
    private final LocalDateTime fecha;

    public Factura(int id, String paciente, String tratamientos, double total, LocalDateTime fecha) {
        this.id = id;
        this.paciente = paciente;
        this.tratamientos = tratamientos;
        this.total = total;
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Factura ID: " + id + 
               "\nPaciente: " + paciente + 
               "\nTotal: $" + total + 
               "\nFecha: " + fecha + 
               "\nTratamientos: " + tratamientos;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getPaciente() {
        return paciente;
    }

    public String getTratamientos() {
        return tratamientos;
    }

    public double getTotal() {
        return total;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}