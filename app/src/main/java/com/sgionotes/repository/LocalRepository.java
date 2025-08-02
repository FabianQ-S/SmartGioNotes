package com.sgionotes.repository;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sgionotes.database.AppDatabase;
import com.sgionotes.database.dao.NoteDao;
import com.sgionotes.database.dao.TagDao;
import com.sgionotes.database.entities.NoteEntity;
import com.sgionotes.database.entities.TagEntity;
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalRepository {

    private NoteDao noteDao;
    private TagDao tagDao;
    private FirebaseAuth mAuth;
    private Gson gson;

    public LocalRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        noteDao = database.noteDao();
        tagDao = database.tagDao();
        mAuth = FirebaseAuth.getInstance();
        gson = new Gson();
    }

    private String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // ==================== OPERACIONES DE NOTAS ====================

    public List<Note> getAllNotes() {
        String userId = getCurrentUserId();
        if (userId == null) return new ArrayList<>();

        List<NoteEntity> entities = noteDao.getAllNotesByUser(userId);
        return convertNotesToModels(entities);
    }

    public List<Note> getTrashNotes() {
        String userId = getCurrentUserId();
        if (userId == null) return new ArrayList<>();

        List<NoteEntity> entities = noteDao.getTrashNotesByUser(userId);
        return convertNotesToModels(entities);
    }

    public Note getNoteById(int noteId) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        NoteEntity entity = noteDao.getNoteById(noteId, userId);
        return entity != null ? convertNoteToModel(entity) : null;
    }

    public long saveNote(Note note) {
        String userId = getCurrentUserId();
        if (userId == null) return -1;

        NoteEntity entity = convertNoteToEntity(note, userId);

        if (note.getId() == 0) {
            // Nueva nota
            return noteDao.insertNote(entity);
        } else {
            // Actualizar nota existente
            entity.setId(note.getId());
            noteDao.updateNote(entity);
            return note.getId();
        }
    }

    public void moveNoteToTrash(int noteId) {
        String userId = getCurrentUserId();
        if (userId != null) {
            noteDao.moveToTrash(noteId, userId, System.currentTimeMillis());
        }
    }

    public void restoreNoteFromTrash(int noteId) {
        String userId = getCurrentUserId();
        if (userId != null) {
            noteDao.restoreFromTrash(noteId, userId, System.currentTimeMillis());
        }
    }

    public void deleteNote(int noteId) {
        String userId = getCurrentUserId();
        if (userId != null) {
            NoteEntity entity = noteDao.getNoteById(noteId, userId);
            if (entity != null) {
                noteDao.deleteNote(entity);
            }
        }
    }

    public void emptyTrash() {
        String userId = getCurrentUserId();
        if (userId != null) {
            noteDao.emptyTrash(userId);
        }
    }

    // ==================== OPERACIONES DE ETIQUETAS ====================

    public List<Tag> getAllTags() {
        String userId = getCurrentUserId();
        if (userId == null) return new ArrayList<>();

        List<TagEntity> entities = tagDao.getAllTagsByUser(userId);
        return convertTagsToModels(entities);
    }

    public List<Tag> getFavoriteTags() {
        String userId = getCurrentUserId();
        if (userId == null) return new ArrayList<>();

        List<TagEntity> entities = tagDao.getFavoriteTagsByUser(userId);
        return convertTagsToModels(entities);
    }

    public Tag getTagById(int tagId) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        TagEntity entity = tagDao.getTagById(tagId, userId);
        return entity != null ? convertTagToModel(entity) : null;
    }

    public Tag getTagByDescription(String description) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        TagEntity entity = tagDao.getTagByDescription(description, userId);
        return entity != null ? convertTagToModel(entity) : null;
    }

    public long saveTag(Tag tag) {
        String userId = getCurrentUserId();
        if (userId == null) return -1;

        TagEntity entity = convertTagToEntity(tag, userId);

        // Verificar si ya existe una etiqueta con esa descripción
        if (tagDao.tagExists(tag.getEtiquetaDescripcion(), userId)) {
            TagEntity existing = tagDao.getTagByDescription(tag.getEtiquetaDescripcion(), userId);
            if (existing != null) {
                existing.setFavorite(tag.isFavorite());
                existing.setFavoriteTimestamp(tag.getFavoriteTimestamp());
                tagDao.updateTag(existing);
                return existing.getId();
            }
        }

        return tagDao.insertTag(entity);
    }

    public void deleteTag(int tagId) {
        String userId = getCurrentUserId();
        if (userId != null) {
            TagEntity entity = tagDao.getTagById(tagId, userId);
            if (entity != null) {
                tagDao.deleteTag(entity);
            }
        }
    }

    public void setTagFavorite(int tagId, boolean isFavorite) {
        String userId = getCurrentUserId();
        if (userId != null) {
            long timestamp = isFavorite ? System.currentTimeMillis() : 0;
            tagDao.setFavorite(tagId, userId, isFavorite, timestamp);
        }
    }

    // ==================== MÉTODOS DE CONVERSIÓN ====================

    private List<Note> convertNotesToModels(List<NoteEntity> entities) {
        List<Note> notes = new ArrayList<>();
        for (NoteEntity entity : entities) {
            notes.add(convertNoteToModel(entity));
        }
        return notes;
    }

    private Note convertNoteToModel(NoteEntity entity) {
        List<Tag> tags = new ArrayList<>();
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> tagNames = gson.fromJson(entity.getTags(), listType);
            for (String tagName : tagNames) {
                tags.add(new Tag(tagName));
            }
        }

        Note note = new Note(
            entity.getId(),
            entity.getTitulo() != null ? entity.getTitulo() : "",
            entity.getContenido() != null ? entity.getContenido() : "",
            tags,
            false, // cortarContenido
            entity.isTrash()
        );

        return note;
    }

    private NoteEntity convertNoteToEntity(Note note, String userId) {
        List<String> tagNames = new ArrayList<>();
        if (note.getEtiquetas() != null) {
            for (Tag tag : note.getEtiquetas()) {
                tagNames.add(tag.getEtiquetaDescripcion());
            }
        }

        String tagsJson = gson.toJson(tagNames);

        NoteEntity entity = new NoteEntity(
            note.getTitulo(),
            note.getContenido(),
            tagsJson,
            note.isTrash(),
            userId
        );

        return entity;
    }

    private List<Tag> convertTagsToModels(List<TagEntity> entities) {
        List<Tag> tags = new ArrayList<>();
        for (TagEntity entity : entities) {
            tags.add(convertTagToModel(entity));
        }
        return tags;
    }

    private Tag convertTagToModel(TagEntity entity) {
        Tag tag = new Tag(entity.getDescripcion());
        tag.setFavorite(entity.isFavorite());
        tag.setFavoriteTimestamp(entity.getFavoriteTimestamp());
        return tag;
    }

    private TagEntity convertTagToEntity(Tag tag, String userId) {
        TagEntity entity = new TagEntity(tag.getEtiquetaDescripcion(), userId);
        entity.setFavorite(tag.isFavorite());
        entity.setFavoriteTimestamp(tag.getFavoriteTimestamp());
        return entity;
    }
}
