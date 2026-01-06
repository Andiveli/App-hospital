package com.example.hospital.data.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Paciente extends Persona implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private TipoSeguro tipoSeguro;
    private ArrayList<String> historialCitas;
    private ArrayList<TratamientoPaciente> historialTratamientos;

    public Paciente(int id, String nombre, String apellido, String correo, String cedula, TipoSeguro tipoSeguro) {
        super(id, nombre, apellido, correo, cedula);
        this.tipoSeguro = tipoSeguro;
        this.historialCitas = new ArrayList<>();
        this.historialTratamientos = new ArrayList<>();
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

    public ArrayList<TratamientoPaciente> getHistorialTratamientos() {
        return historialTratamientos;
    }

    public void setHistorialTratamientos(ArrayList<TratamientoPaciente> historialTratamientos) {
        this.historialTratamientos = historialTratamientos;
    }

    public void agregarTratamiento(TratamientoPaciente tratamiento) {
        if (this.historialTratamientos == null) {
            this.historialTratamientos = new ArrayList<>();
        }
        this.historialTratamientos.add(tratamiento);
    }

    public double getCostoTotalTratamientos() {
        double total = 0;
        if (historialTratamientos != null) {
            for (TratamientoPaciente tp : historialTratamientos) {
                if ("ACTIVO".equals(tp.getEstado())) {
                    total += tp.getCostoTotal();
                }
            }
        }
        return total;
    }

    public int getCantidadTratamientosActivos() {
        int count = 0;
        if (historialTratamientos != null) {
            for (TratamientoPaciente tp : historialTratamientos) {
                if ("ACTIVO".equals(tp.getEstado())) {
                    count++;
                }
            }
        }
        return count;
    }

}