package com.funeraria.funeraria.common.entities;

/**
 * Created by ZeptooUser on 26/03/2017.
 */

public class Restos {

    private int idLugarRestos;
    private String nombre;
    private String ubicacion;
    private String activo;
    private String borrado;

    public Restos(){

    }

    public int getIdLugarRestos() {
        return idLugarRestos;
    }

    public void setIdLugarRestos(int idLugarRestos) {
        this.idLugarRestos = idLugarRestos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }

    public String getBorrado() {
        return borrado;
    }

    public void setBorrado(String borrado) {
        this.borrado = borrado;
    }
}
