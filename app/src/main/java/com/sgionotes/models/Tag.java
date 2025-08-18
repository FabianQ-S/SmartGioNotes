package com.sgionotes.models;

public class Tag {
    private String id;
    private String etiquetaDescripcion;
    private boolean isFavorite;
    private long favoriteTimestamp;
    private String userId;

    public Tag() {
    }

    public Tag(String etiquetaDescripcion) {
        this.etiquetaDescripcion = etiquetaDescripcion;
        this.isFavorite = false;
        this.favoriteTimestamp = 0;
    }

    public Tag(String id, String etiquetaDescripcion, String userId) {
        this.id = id;
        this.etiquetaDescripcion = etiquetaDescripcion;
        this.userId = userId;
        this.isFavorite = false;
        this.favoriteTimestamp = 0;
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

    public String getEtiquetaDescripcion() {
        return etiquetaDescripcion;
    }

    public void setEtiquetaDescripcion(String etiquetaDescripcion) {
        this.etiquetaDescripcion = etiquetaDescripcion;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        if (favorite) {
            this.favoriteTimestamp = System.currentTimeMillis();
        } else {
            this.favoriteTimestamp = 0;
        }
    }

    public long getFavoriteTimestamp() {
        return favoriteTimestamp;
    }

    public void setFavoriteTimestamp(long favoriteTimestamp) {
        this.favoriteTimestamp = favoriteTimestamp;
    }

    public String getDisplayText() {
        return etiquetaDescripcion;
    }
}
