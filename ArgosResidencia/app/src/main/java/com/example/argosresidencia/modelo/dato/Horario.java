package com.example.argosresidencia.modelo.dato;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Horario implements Serializable {
    public String getActividad() {
        return nombreActividad;
    }

    public void setActividad(String nombreActividad) {
        this.nombreActividad = nombreActividad;
    }

    public boolean esActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @NotNull
    @Override
    public String toString() {
        return this.nombreActividad;
    }

    private String nombreActividad;

    private boolean activo;

    public Horario(String nombreActividad)  {
        this.nombreActividad = nombreActividad;
        this.activo = false;
    }

    public Horario(String nombreActividad, boolean activo)  {
        this.nombreActividad = nombreActividad;
        this.activo = activo;
    }
}

