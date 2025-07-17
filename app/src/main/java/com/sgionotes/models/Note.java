package com.sgionotes.models;

import java.util.List;

public class Note {

    private String titulo;
    private String contenido;
    private List<Tag> tags;

    public Note(String titulo, String contenido, List<Tag> tags,
                boolean cortarContenido) {
        this.titulo = titulo;
        this.contenido = cortarContenido(contenido, cortarContenido);
        this.tags = tags;
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

    public List<Tag> getEtiquetas() {
        return tags;
    }

    public void setEtiquetas(List<Tag> tags) {
        this.tags = tags;
    }

    private String cortarContenido(String contenido, boolean swt) {
        if (contenido.length() > 190 && swt)
            return contenido.substring(0, 190) + "...";
        return contenido;
    }

}
