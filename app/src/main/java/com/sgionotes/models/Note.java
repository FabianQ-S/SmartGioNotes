package com.sgionotes.models;

import java.util.List;

public class Note {

    private String titulo;
    private String contenido;
    private List<Etiqueta> etiquetas;

    public Note(String titulo, String contenido, List<Etiqueta> etiquetas,
                boolean cortarContenido) {
        this.titulo = titulo;
        this.contenido = cortarContenido(contenido, cortarContenido);
        this.etiquetas = etiquetas;
    }

    public Note(String contenido) {
        this.contenido = contenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<Etiqueta> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(List<Etiqueta> etiquetas) {
        this.etiquetas = etiquetas;
    }

    private String cortarContenido(String contenido, boolean swt) {
        if (contenido.length() > 190 && swt)
            return contenido.substring(0, 190) + "...";
        return contenido;
    }

}
