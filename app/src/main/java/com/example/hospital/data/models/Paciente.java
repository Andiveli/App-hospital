package com.example.hospital.data.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Paciente extends Persona implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private TipoSeguro tipoSeguro;
    private ArrayList<String> historialCitas;

    public Paciente(int id, String nombre, String apellido, String correo, String cedula, TipoSeguro tipoSeguro) {
        super(id, nombre, apellido, correo, cedula);
        this.tipoSeguro = tipoSeguro;
        this.historialCitas = new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Paciente paciente = (Paciente) obj;
        return this.getCedulaString().equals(paciente.getCedulaString());
    }

    @Override
    public int hashCode() {
        return this.getCedulaString().hashCode();
    }

    @Override
    public String toString() {
        return "Nombre: " + getNombre() + " " + getApellido() + 
               ", Cédula: " + getCedulaString() + 
               ", Correo: " + getCorreo() + 
               ", Tipo de Seguro: " + tipoSeguro;
    }

    public TipoSeguro getTipoSeguro() {
        return tipoSeguro;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setTipoSeguro(TipoSeguro tipoSeguro) {
        this.tipoSeguro = tipoSeguro;
    }

    public ArrayList<String> getHistorialCitas() {
        return historialCitas;
    }

    public void setHistorialCitas(ArrayList<String> historialCitas) {
        this.historialCitas = historialCitas;
    }

}