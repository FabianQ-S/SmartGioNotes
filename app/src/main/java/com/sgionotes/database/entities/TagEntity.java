package com.sgionotes.database.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tags")
public class TagEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String descripcion;
    private boolean isFavorite;
    private long favoriteTimestamp;
    private String userId; //UsuarioPropietario

    public TagEntity() {}

    @Ignore
    public TagEntity(String descripcion, String userId) {
        this.descripcion = descripcion;
        this.userId = userId;
        this.isFavorite = false;
        this.favoriteTimestamp = 0;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }

    public long getFavoriteTimestamp() { return favoriteTimestamp; }
    public void setFavoriteTimestamp(long favoriteTimestamp) { this.favoriteTimestamp = favoriteTimestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
