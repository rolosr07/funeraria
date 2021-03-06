package com.funeraria.funeraria.common.entities;

/**
 * Created by ZeptooUser on 26/03/2017.
 */

public class Servicio {

    private int idServicio;
    private int idTipoServicio;
    private String nombre;
    private String precio;
    private String imagen;
    private String texto;
    private String tiempoMostrar;
    private String activo;
    private String borrado;
    private String fechaCreacion;

    private String nombreUsuario;
    private String apellidoUsuario;
    private String fechaCompra;
    private String autorizado;
    private int idServicioComprado;

    public Servicio(){
    }

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public int getIdTipoServicio() {
        return idTipoServicio;
    }

    public void setIdTipoServicio(int idTipoServicio) {
        this.idTipoServicio = idTipoServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getTiempoMostrar() {
        return tiempoMostrar;
    }

    public void setTiempoMostrar(String tiempoMostrar) {
        this.tiempoMostrar = tiempoMostrar;
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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }

    public String getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(String fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getAutorizado() {
        return autorizado;
    }

    public void setAutorizado(String autorizado) {
        this.autorizado = autorizado;
    }

    public int getIdServicioComprado() {
        return idServicioComprado;
    }

    public void setIdServicioComprado(int idServicioComprado) {
        this.idServicioComprado = idServicioComprado;
    }
}
