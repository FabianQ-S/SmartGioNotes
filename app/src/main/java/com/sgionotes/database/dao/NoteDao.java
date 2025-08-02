package com.sgionotes.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.sgionotes.database.entities.NoteEntity;
import java.util.List;

@Dao
public interface NoteDao {

    // Obtener todas las notas de un usuario (excluyendo papelera)
    @Query("SELECT * FROM notes WHERE userId = :userId AND isTrash = 0 ORDER BY updatedAt DESC")
    List<NoteEntity> getAllNotesByUser(String userId);

    // Obtener notas en papelera de un usuario
    @Query("SELECT * FROM notes WHERE userId = :userId AND isTrash = 1 ORDER BY updatedAt DESC")
    List<NoteEntity> getTrashNotesByUser(String userId);

    // Obtener todas las notas de un usuario (incluyendo papelera)
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY updatedAt DESC")
    List<NoteEntity> getAllNotesIncludingTrash(String userId);

    // Buscar notas por título o contenido
    @Query("SELECT * FROM notes WHERE userId = :userId AND isTrash = 0 AND (titulo LIKE :searchQuery OR contenido LIKE :searchQuery) ORDER BY updatedAt DESC")
    List<NoteEntity> searchNotes(String userId, String searchQuery);

    // Obtener una nota específica
    @Query("SELECT * FROM notes WHERE id = :noteId AND userId = :userId")
    NoteEntity getNoteById(int noteId, String userId);

    // Insertar nueva nota
    @Insert
    long insertNote(NoteEntity note);

    // Actualizar nota existente
    @Update
    void updateNote(NoteEntity note);

    // Eliminar nota (físicamente)
    @Delete
    void deleteNote(NoteEntity note);

    // Mover nota a papelera
    @Query("UPDATE notes SET isTrash = 1, updatedAt = :timestamp WHERE id = :noteId AND userId = :userId")
    void moveToTrash(int noteId, String userId, long timestamp);

    // Restaurar nota de papelera
    @Query("UPDATE notes SET isTrash = 0, updatedAt = :timestamp WHERE id = :noteId AND userId = :userId")
    void restoreFromTrash(int noteId, String userId, long timestamp);

    // Eliminar todas las notas en papelera de un usuario
    @Query("DELETE FROM notes WHERE userId = :userId AND isTrash = 1")
    void emptyTrash(String userId);

    // Obtener conteo de notas de un usuario
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isTrash = 0")
    int getNotesCount(String userId);
}
