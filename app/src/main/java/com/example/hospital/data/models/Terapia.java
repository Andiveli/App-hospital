package com.example.hospital.data.models;

public class Terapia extends Tratamiento {
    private static final long serialVersionUID = 1L;

    public Terapia(String nombre, int duracion, double precio) {
        super(nombre, duracion, precio);
    }

    @Override
    public String getTipo() {
        return "terapia";
    }

    @Override
    public double calcularCosto() {
        double costoBase = getCostoBase();
        double recargo = 0.15 * getPrecio(); // Recargo del 15% para terapias
        if(getDuracion() > 30) {
            recargo *= 0.70; // Descuento del 30% si la duración es mayor a 30 horas
        }
        return costoBase + recargo;
    }

    @Override
    public String toString() {
        return "Terapia: " + getNombre() + 
               ", Sesiones: " + getDuracion() + 
               ", Precio por sesión: $" + getPrecio();
    }
}