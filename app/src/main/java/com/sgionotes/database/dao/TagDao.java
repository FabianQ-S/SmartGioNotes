package com.sgionotes.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.sgionotes.database.entities.TagEntity;
import java.util.List;

@Dao
public interface TagDao {

    // Obtener todas las etiquetas de un usuario
    @Query("SELECT * FROM tags WHERE userId = :userId ORDER BY isFavorite DESC, favoriteTimestamp ASC, descripcion ASC")
    List<TagEntity> getAllTagsByUser(String userId);

    // Obtener etiquetas favoritas de un usuario
    @Query("SELECT * FROM tags WHERE userId = :userId AND isFavorite = 1 ORDER BY favoriteTimestamp ASC")
    List<TagEntity> getFavoriteTagsByUser(String userId);

    // Buscar etiquetas por descripción
    @Query("SELECT * FROM tags WHERE userId = :userId AND descripcion LIKE :searchQuery ORDER BY isFavorite DESC, descripcion ASC")
    List<TagEntity> searchTags(String userId, String searchQuery);

    // Obtener una etiqueta específica
    @Query("SELECT * FROM tags WHERE id = :tagId AND userId = :userId")
    TagEntity getTagById(int tagId, String userId);

    // Obtener etiqueta por descripción
    @Query("SELECT * FROM tags WHERE descripcion = :descripcion AND userId = :userId")
    TagEntity getTagByDescription(String descripcion, String userId);

    // Insertar nueva etiqueta
    @Insert
    long insertTag(TagEntity tag);

    // Actualizar etiqueta existente
    @Update
    void updateTag(TagEntity tag);

    // Eliminar etiqueta
    @Delete
    void deleteTag(TagEntity tag);

    // Marcar/desmarcar etiqueta como favorita
    @Query("UPDATE tags SET isFavorite = :isFavorite, favoriteTimestamp = :timestamp WHERE id = :tagId AND userId = :userId")
    void setFavorite(int tagId, String userId, boolean isFavorite, long timestamp);

    // Obtener conteo de etiquetas de un usuario
    @Query("SELECT COUNT(*) FROM tags WHERE userId = :userId")
    int getTagsCount(String userId);

    // Verificar si existe una etiqueta con esa descripción
    @Query("SELECT EXISTS(SELECT 1 FROM tags WHERE descripcion = :descripcion AND userId = :userId)")
    boolean tagExists(String descripcion, String userId);
}
