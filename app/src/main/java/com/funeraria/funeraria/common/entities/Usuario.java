package com.funeraria.funeraria.common.entities;

/**
 * Created by ZeptooUser on 01/04/2017.
 */

public class Usuario {

    private int idUsuario;
    private String nombre;
    private String apellido;
    private String userName;
    private String password;
    private String rol;
    private String email;
    private String activo;
    private String borrado;
    private String fechaCreacion;
    private String autorizado;
    private String fechaAutorizacion;
    private int idUsuarioAutorizado;
    private int idDifunto;

    public Usuario(){

    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getAutorizado() {
        return autorizado;
    }

    public void setAutorizado(String autorizado) {
        this.autorizado = autorizado;
    }

    public String getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(String fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public int getIdUsuarioAutorizado() {
        return idUsuarioAutorizado;
    }

    public void setIdUsuarioAutorizado(int idUsuarioAutorizado) {
        this.idUsuarioAutorizado = idUsuarioAutorizado;
    }

    public int getIdDifunto() {
        return idDifunto;
    }

    public void setIdDifunto(int idDifunto) {
        this.idDifunto = idDifunto;
    }
}
