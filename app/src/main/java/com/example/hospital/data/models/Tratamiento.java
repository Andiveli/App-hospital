package com.example.hospital.data.models;

import java.io.Serializable;

public abstract class Tratamiento implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double COSTO_BASE = 50.0;
    
    private String nombre;
    private int duracion;
    private double precio;

    public Tratamiento(String nombre, int duracion, double precio) {
        this.nombre = nombre;
        this.duracion = duracion;
        this.precio = precio;
    }   

    public static double getCostoBase() {
        return COSTO_BASE;
    }

    public double getPrecio() {
        return precio;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDuracion() {
        return duracion;
    }

    public double pagar() {
        return calcularCosto();
    }

    @Override
    public String toString() {
        return "Tratamiento: " + nombre + 
               ", Duración: " + duracion + " hora(s), " + 
               "Precio por hora: $" + precio;
    }

    public abstract double calcularCosto();
    public abstract String getTipo();
}