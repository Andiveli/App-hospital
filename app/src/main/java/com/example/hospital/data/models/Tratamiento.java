package com.example.hospital.data.models;

import java.io.Serializable;

public abstract class Tratamiento implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double COSTO_BASE = 50.0;
    
    private String nombre;
    private int duracion;
    private double precio;
    private int id;

    public Tratamiento(String nombre, int duracion, double precio) {
        this.nombre = nombre;
        this.duracion = duracion;
        this.precio = precio;
        this.id = 0; // Se asignará después
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

    public int getId() {
        return id;
    }

    public void setnuevoId(int id) {
        this.id = id;
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