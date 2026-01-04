package com.example.hospital.data.models;

import java.io.Serializable;

public abstract class Persona implements Serializable {
    protected int id;
    protected String nombre;
    protected String apellido;
    protected String correo;
    protected String cedulaString;

    public Persona(int id, String nombre, String apellido, String correo, String cedulaString) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.cedulaString = cedulaString;
    }

    public String getCedulaString() {
        return cedulaString;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setCedulaString(String cedulaString) {
        this.cedulaString = cedulaString;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Nombre: " + nombre + " " + apellido + 
               ", Correo: " + correo + ", Cédula: " + cedulaString;
    }
}