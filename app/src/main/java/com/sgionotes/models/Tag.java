package com.sgionotes.models;

public class Tag {
    private String etiquetaDescripcion;
    private boolean isFavorite;
    private long favoriteTimestamp;

    public Tag(String etiquetaDescripcion) {
        this.etiquetaDescripcion = etiquetaDescripcion;
        this.isFavorite = false;
        this.favoriteTimestamp = 0;
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
