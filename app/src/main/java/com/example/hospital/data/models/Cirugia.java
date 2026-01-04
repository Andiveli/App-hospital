package com.example.hospital.data.models;

public class Cirugia extends Tratamiento {
    private static final long serialVersionUID = 1L;

    public Cirugia(String nombre, int duracion, double precio) {
        super(nombre, duracion, precio);
    }

    @Override
    public String getTipo() {
        return "cirugia";
    }

    @Override
    public double calcularCosto() {
        double costoBase = getCostoBase();
        double recargo = 0.25 * getPrecio(); // Recargo del 25% para cirugías
        return costoBase + recargo;
    }

    @Override
    public String toString() {
        return "Cirugia: " + getNombre() + 
               ", Duración: " + getDuracion() + " hora(s), " + 
               "Precio: $" + getPrecio();
    }
}
