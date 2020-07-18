package com.example.argosresidencia.modelo.dato;

import android.app.Application;
import android.content.SharedPreferences;

public class Sesion extends Application {

    private SharedPreferences preferencias;

    private static Sesion sesion;

    private int tipoUsuario;   //1=alumno 2=monitor 0=usuario_indefinido
    private String noControl;
    private String correo;

    private Sesion() { }

    public static Sesion getInstance() {
        if(sesion == null){
            sesion = new Sesion();
        }
        return sesion;
    }

    public void setTipoUsuario(Integer tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public int getTipoUsuario() {
        return tipoUsuario;
    }

    public  void setNoControl(String noControl) {
        this.noControl = noControl;
    }

    public String getNoControl(){
        return noControl;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }

    public void guardarPerfil() {
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("noControl", noControl);
        editor.putInt("tipoUsuario", tipoUsuario);
    }

    public void cargarPerfil() {
        tipoUsuario = preferencias.getInt("tipoUsuario",0);
        noControl = preferencias.getString("noControl",null);
    }

    public void limpiarPerfil() {
        SharedPreferences.Editor editor = preferencias.edit();
        editor.remove("tipoUsuario");
        editor.remove("noControl");
        editor.commit();
    }
}
