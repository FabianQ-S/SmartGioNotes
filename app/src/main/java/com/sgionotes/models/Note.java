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
    private String gpsLocation;
    public Note() {
    }
    public Note(String titulo, String contenido) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.isFavorite = false;
        this.isTrash = false;
        this.timestamp = System.currentTimeMillis();
        this.tagIds = new ArrayList<>();
        this.gpsLocation = null; //InicializarGPS
    }
    public Note(Note other) {
        if (other != null) {
            this.id = other.id;
            this.titulo = other.titulo;
            this.contenido = other.contenido;
            this.tagIds = other.tagIds != null ? new ArrayList<>(other.tagIds) : new ArrayList<>();
            this.isFavorite = other.isFavorite;
            this.isTrash = other.isTrash;
            this.timestamp = other.timestamp;
            this.userId = other.userId;
            this.gpsLocation = other.gpsLocation;
        } else {
            this.titulo = "";
            this.contenido = "";
            this.isFavorite = false;
            this.isTrash = false;
            this.timestamp = System.currentTimeMillis();
            this.tagIds = new ArrayList<>();
            this.gpsLocation = null;
        }
    }
    public Note(int legacyId, String titulo, String contenido, List<Tag> etiquetas,
                boolean cortarContenido, boolean isTrash) {
        this.titulo = titulo;
        this.contenido = cortarContenido(contenido, cortarContenido);
        this.isTrash = isTrash;
        this.isFavorite = false;
        this.timestamp = System.currentTimeMillis();
        this.tagIds = new ArrayList<>();

        //EtiquetasID
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
        // Garantizar que nunca devolvemos null para evitar problemas de serialización en Firebase
        if (tagIds == null) {
            tagIds = new ArrayList<>();
        }
        return tagIds;
    }

    public void setTagIds(List<String> tagIds) {
        // Garantizar que siempre tenemos una lista válida
        if (tagIds == null) {
            this.tagIds = new ArrayList<>();
        } else {
            this.tagIds = new ArrayList<>(tagIds); // Crear copia defensiva
        }
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean getIsFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public void setIsFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public boolean getIsTrash() {
        return isTrash;
    }

    public void setTrash(boolean trash) {
        this.isTrash = trash;
    }

    public void setIsTrash(boolean trash) {
        this.isTrash = trash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public List<Tag> getEtiquetas() {
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
        // MetodoDeCompatibilidad
        return getEtiquetas();
    }
    public void setTags(List<Tag> tags) {
        setEtiquetas(tags);
    }

    // Utilidad
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
