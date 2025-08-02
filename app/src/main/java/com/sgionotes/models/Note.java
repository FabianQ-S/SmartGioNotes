package com.sgionotes.models;

import java.util.List;
import java.util.ArrayList;

public class Note {
    private String id;
    private String titulo;
    private String contenido;
    private List<String> tagIds;
    private boolean isFavorite;
    private boolean isTrash;
    private long timestamp;
    private String userId;

    // Constructor vacío requerido por Firebase
    public Note() {
    }

    public Note(String titulo, String contenido) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.isFavorite = false;
        this.isTrash = false;
        this.timestamp = System.currentTimeMillis();
        this.tagIds = new ArrayList<>();
    }

    // Constructor de compatibilidad con el código existente
    public Note(int legacyId, String titulo, String contenido, List<Tag> etiquetas,
                boolean cortarContenido, boolean isTrash) {
        this.titulo = titulo;
        this.contenido = cortarContenido(contenido, cortarContenido);
        this.isTrash = isTrash;
        this.isFavorite = false;
        this.timestamp = System.currentTimeMillis();
        this.tagIds = new ArrayList<>();

        // Convertir etiquetas a IDs
        if (etiquetas != null) {
            for (Tag tag : etiquetas) {
                if (tag.getId() != null) {
                    this.tagIds.add(tag.getId());
                }
            }
        }
    }

    public Note(String id, String titulo, String contenido, List<String> tagIds,
                boolean isFavorite, boolean isTrash, String userId) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.tagIds = tagIds;
        this.isFavorite = isFavorite;
        this.isTrash = isTrash;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public List<String> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<String> tagIds) {
        this.tagIds = tagIds;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setTrash(boolean trash) {
        this.isTrash = trash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Métodos de compatibilidad con código existente
    public List<Tag> getEtiquetas() {
        // Retorna una lista vacía por compatibilidad
        // En el nuevo sistema, las etiquetas se manejan por IDs
        return new ArrayList<>();
    }

    public void setEtiquetas(List<Tag> etiquetas) {
        this.tagIds = new ArrayList<>();
        if (etiquetas != null) {
            for (Tag tag : etiquetas) {
                if (tag.getId() != null) {
                    this.tagIds.add(tag.getId());
                }
            }
        }
    }

    public List<Tag> getTags() {
        // Método de compatibilidad
        return getEtiquetas();
    }

    public void setTags(List<Tag> tags) {
        setEtiquetas(tags);
    }

    // Métodos de utilidad
    public String getContenidoRecortado() {
        if (contenido != null && contenido.length() > 190) {
            return contenido.substring(0, 190) + "...";
        }
        return contenido;
    }

    private String cortarContenido(String contenido, boolean swt) {
        if (contenido != null && contenido.length() > 190 && swt) {
            return contenido.substring(0, 190) + "...";
        }
        return contenido;
    }
}
