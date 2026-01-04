package com.example.hospital.data.models;

public class Medicacion extends Tratamiento {
    private static final long serialVersionUID = 1L;

    public Medicacion(String nombre, int duracion, double precio) {
        super(nombre, duracion, precio);
    }

    @Override
    public String getTipo() {
        return "medicacion";
    }
    
    @Override
    public double calcularCosto() {
        double suma = getCostoBase();
        if(getDuracion() > 5) {
            suma += getPrecio() * getDuracion() * 0.9;
        } else {
            suma += getPrecio() * getDuracion();
        }
        return suma;
    }

    @Override
    public String toString() {
        return "Medicacion: " + getNombre() + 
               ", Duración: " + getDuracion() + " días, " + 
               "Precio por día: $" + getPrecio();
    }
}