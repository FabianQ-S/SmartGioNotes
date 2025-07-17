package com.sgionotes.models;

import java.util.List;

public class Note {

    private int id;
    private String titulo;
    private String contenido;
    private List<Tag> tags;
    private boolean isTrash;

    public Note(int id, String titulo, String contenido, List<Tag> tags,
                boolean cortarContenido, boolean isTrash) {
        this.titulo = titulo;
        this.contenido = cortarContenido(contenido, cortarContenido);
        this.tags = tags;
        this.id = id;
        this.isTrash = isTrash;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setTrash(boolean trash) {
        isTrash = trash;
    }

    public int getId() {
        return id;
    }

    public List<Tag> getTags() {
        return tags;
    }

    private String cortarContenido(String contenido, boolean swt) {
        if (contenido.length() > 190 && swt)
            return contenido.substring(0, 190) + "...";
        return contenido;
    }

}
