package com.sgionotes.models;

public class Tag {
    private String etiquetaDescripcion;

    public Tag(String etiquetaDescripcion) {
        this.etiquetaDescripcion = etiquetaDescripcion;
    }

    public String getEtiquetaDescripcion() {
        return etiquetaDescripcion;
    }

    public void setEtiquetaDescripcion(String etiquetaDescripcion) {
        this.etiquetaDescripcion = etiquetaDescripcion;
    }
}
