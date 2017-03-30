package com.funeraria.funeraria.common;

/**
 * Created by Rolo on 25/03/2017.
 */
public class Difunto {

    private int idDifunto;
    private String nombre;
    private String apellidos;
    private String fechaNacimiento;
    private String fechaDefuncion;
    private String activo;
    private String borrado;
    private String fechaCreacion;

    public Difunto(int idDifunto, String nombre) {
        this.idDifunto = idDifunto;
        this.nombre = nombre;
    }


    public int getIdDifunto() {
        return idDifunto;
    }

    public void setIdDifunto(int idDifunto) {
        this.idDifunto = idDifunto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String name) {
        this.nombre = name;
    }


    //to display object as a string in spinner
    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Difunto){
            Difunto c = (Difunto )obj;
            if(c.getNombre().equals(nombre) && c.getIdDifunto()==idDifunto ) return true;
        }

        return false;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getFechaDefuncion() {
        return fechaDefuncion;
    }

    public void setFechaDefuncion(String fechaDefuncion) {
        this.fechaDefuncion = fechaDefuncion;
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

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}