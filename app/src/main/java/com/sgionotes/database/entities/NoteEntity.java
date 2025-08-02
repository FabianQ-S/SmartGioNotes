package com.sgionotes.database.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String titulo;
    private String contenido;
    private String tags;
    private boolean isTrash;
    private long createdAt;
    private long updatedAt;
    private String userId;

    public NoteEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public NoteEntity(String titulo, String contenido, String tags, boolean isTrash, String userId) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.tags = tags;
        this.isTrash = isTrash;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) {
        this.contenido = contenido;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getTags() { return tags; }
    public void setTags(String tags) {
        this.tags = tags;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isTrash() { return isTrash; }
    public void setTrash(boolean trash) {
        isTrash = trash;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
