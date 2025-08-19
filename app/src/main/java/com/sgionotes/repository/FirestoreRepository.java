package com.sgionotes.repository;
import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;
import com.sgionotes.models.GenerarData;
import java.util.ArrayList;
import java.util.List;
public class FirestoreRepository {
    private static final String TAG = "FirestoreRepository";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_NOTES = "notes";
    private static final String COLLECTION_TAGS = "tags";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private Context context;
    private String currentUserId = null;
    private String previousUserId = null;
    private FirebaseAuth.AuthStateListener authStateListener;
    public FirestoreRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        mAuth = FirebaseAuth.getInstance();
        this.context = context;
        setupAuthStateListener();
    }
    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String newUserId = user != null ? user.getUid() : null;
            if (!java.util.Objects.equals(currentUserId, newUserId)) {
                Log.d(TAG, "Usuario cambió de: " + currentUserId + " a: " + newUserId);
                if (currentUserId != null && newUserId != null && !currentUserId.equals(newUserId)) {
                    previousUserId = currentUserId;
                    currentUserId = newUserId;
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            GenerarData.getInstancia().onUserChanged(currentUserId);
                        });
                    }
                } else if (currentUserId == null && newUserId != null) {
                    //EstablecerUsuario
                    currentUserId = newUserId;
                    Log.d(TAG, "Usuario inicial establecido: " + newUserId);
                } else if (currentUserId != null && newUserId == null) {
                    //LogoutUsuario
                    previousUserId = currentUserId;
                    currentUserId = null;
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            GenerarData.getInstancia().clearUserData();
                        });
                    }
                } else {
                    currentUserId = newUserId;
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);
    }
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        if (!java.util.Objects.equals(currentUserId, userId)) {
            Log.d(TAG, "Usuario detectado como cambiado en getCurrentUserId(): " + currentUserId + " -> " + userId);
            previousUserId = currentUserId;
            currentUserId = userId;
        }
        return userId;
    }
    public String getPreviousUserId() {
        return previousUserId;
    }
    //LimpiarRepsositorio
    public void cleanup() {
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
    public void getAllTags(DataCallback<List<Tag>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        Log.d(TAG, "Obteniendo tags para usuario: " + userId);
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TAGS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Tag> tags = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tag tag = document.toObject(Tag.class);
                        tag.setId(document.getId());
                        tags.add(tag);
                    }
                    tags.sort((t1, t2) -> {
                        if (t1.isFavorite() != t2.isFavorite()) {
                            return Boolean.compare(t2.isFavorite(), t1.isFavorite());
                        }
                        return t1.getEtiquetaDescripcion().compareToIgnoreCase(t2.getEtiquetaDescripcion());
                    });
                    Log.d(TAG, "Tags obtenidos exitosamente: " + tags.size() + " para usuario: " + userId);
                    callback.onSuccess(tags);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo tags para usuario: " + userId, e);
                    callback.onError("Error al obtener etiquetas: " + e.getMessage());
                });
    }
    public void getFavoriteTags(DataCallback<List<Tag>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TAGS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Tag> tags = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tag tag = document.toObject(Tag.class);
                        tag.setId(document.getId());
                        if (tag.isFavorite()) {
                            tags.add(tag);
                        }
                    }
                    tags.sort((t1, t2) -> Long.compare(t1.getFavoriteTimestamp(), t2.getFavoriteTimestamp()));

                    callback.onSuccess(tags);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo tags favoritos", e);
                    callback.onError("Error al obtener etiquetas favoritas: " + e.getMessage());
                });
    }
    public void saveTag(Tag tag, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Error: Usuario no autenticado");
            callback.onError("Usuario no autenticado");
            return;
        }
        Log.d(TAG, "Guardando tag: " + tag.getEtiquetaDescripcion() + " para usuario: " + userId);
        if (tag.getId() == null || tag.getId().isEmpty()) {
            Log.d(TAG, "Creando nueva etiqueta en Firestore");
            db.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_TAGS)
                    .add(tag)
                    .addOnSuccessListener(documentReference -> {
                        tag.setId(documentReference.getId());
                        Log.d(TAG, "Tag creado exitosamente con ID: " + documentReference.getId());
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creando tag en Firestore", e);
                        callback.onError("Error al crear etiqueta: " + e.getMessage());
                    });
        } else {
            Log.d(TAG, "Actualizando etiqueta existente con ID: " + tag.getId());
            db.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_TAGS)
                    .document(tag.getId())
                    .set(tag)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tag actualizado exitosamente");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error actualizando tag en Firestore", e);
                        callback.onError("Error al actualizar etiqueta: " + e.getMessage());
                    });
        }
    }
    public void deleteTag(String tagId, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || tagId == null || tagId.isEmpty()) {
            callback.onError("Usuario no autenticado o ID de etiqueta inválido");
            return;
        }
        removeTagFromAllNotes(tagId, new SimpleCallback() {
            @Override
            public void onSuccess() {
                // EliminarLaEtiqueta
                db.collection(COLLECTION_USERS)
                        .document(userId)
                        .collection(COLLECTION_TAGS)
                        .document(tagId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Tag eliminado exitosamente");
                            if (context instanceof android.app.Activity) {
                                ((android.app.Activity) context).runOnUiThread(() -> {
                                    GenerarData generarData = GenerarData.getInstancia();
                                    generarData.cleanupDeletedTagReferences(tagId);
                                    generarData.refreshData();
                                });
                            }

                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error eliminando tag", e);
                            callback.onError("Error al eliminar etiqueta: " + e.getMessage());
                        });
            }
            @Override
            public void onError(String error) {
                callback.onError("Error preparando eliminación: " + error);
            }
        });
    }
    // MetodoAuxiliarEliminarEtiqueta
    private void removeTagFromAllNotes(String tagId, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        // ObtenerNotas
        getAllNotesIncludingTrash(new DataCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                List<Note> notesToUpdate = new ArrayList<>();
                for (Note note : notes) {
                    List<String> tagIds = note.getTagIds();
                    if (tagIds != null && tagIds.contains(tagId)) {
                        List<String> newTagIds = new ArrayList<>(tagIds);
                        newTagIds.remove(tagId);
                        note.setTagIds(newTagIds);
                        notesToUpdate.add(note);
                    }
                }
                if (notesToUpdate.isEmpty()) {
                    callback.onSuccess();
                    return;
                }
                updateNotesInBatch(notesToUpdate, 0, callback);
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // MetodoAuxiliarActualizarNotas
    private void updateNotesInBatch(List<Note> notes, int index, SimpleCallback callback) {
        if (index >= notes.size()) {
            callback.onSuccess();
            return;
        }
        Note note = notes.get(index);
        saveNote(note, new SimpleCallback() {
            @Override
            public void onSuccess() {
                updateNotesInBatch(notes, index + 1, callback);
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error actualizando nota durante eliminación de etiqueta: " + error);
                updateNotesInBatch(notes, index + 1, callback);
            }
        });
    }
    public void setTagFavorite(String tagId, boolean isFavorite, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || tagId == null || tagId.isEmpty()) {
            callback.onError("Usuario no autenticado o ID de etiqueta inválido");
            return;
        }
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TAGS)
                .document(tagId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Tag tag = documentSnapshot.toObject(Tag.class);
                        if (tag != null) {
                            tag.setId(documentSnapshot.getId());
                            tag.setFavorite(isFavorite);
                            db.collection(COLLECTION_USERS)
                                    .document(userId)
                                    .collection(COLLECTION_TAGS)
                                    .document(tagId)
                                    .set(tag)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Tag favorito actualizado");
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error actualizando favorito", e);
                                        callback.onError("Error al actualizar favorito: " + e.getMessage());
                                    });
                        } else {
                            callback.onError("Error al obtener datos de la etiqueta");
                        }
                    } else {
                        callback.onError("Etiqueta no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo tag", e);
                    callback.onError("Error al obtener etiqueta: " + e.getMessage());
                });
    }
    public void getAllNotesIncludingTrash(DataCallback<List<Note>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        Log.d(TAG, "Obteniendo todas las notas (incluida papelera) para usuario: " + userId);
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTES)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Note> notes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Note note = document.toObject(Note.class);
                        note.setId(document.getId());
                        List<String> tagIds = note.getTagIds();

                        // Log detallado para debugging de etiquetas
                        Log.w("TAG_DEBUG", String.format("[FIREBASE_LOAD] NoteID:%s | TagIds from DB: %s (Count:%d)",
                            note.getId(),
                            tagIds != null ? String.join(",", tagIds) : "NULL",
                            tagIds != null ? tagIds.size() : 0));

                        Log.d(TAG, "Nota procesada: " + note.getId() + " isTrash: " + note.isTrash() +
                              " tags: " + (tagIds != null ? tagIds.size() : 0));
                        notes.add(note);
                    }
                    notes.sort((n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                    Log.d(TAG, "Todas las notas obtenidas exitosamente: " + notes.size() + " para usuario: " + userId);
                    callback.onSuccess(notes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo todas las notas para usuario: " + userId, e);
                    callback.onError("Error al obtener notas: " + e.getMessage());
                });
    }
    public void getAllNotes(DataCallback<List<Note>> callback) {
        getAllNotesIncludingTrash(new DataCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> allNotes) {
                List<Note> activeNotes = new ArrayList<>();
                for (Note note : allNotes) {
                    if (!note.isTrash()) {
                        activeNotes.add(note);
                    }
                }
                callback.onSuccess(activeNotes);
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    public void getTrashNotes(DataCallback<List<Note>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Note> notes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Note note = document.toObject(Note.class);
                        note.setId(document.getId());
                        //papelera
                        if (note.isTrash()) {
                            notes.add(note);
                        }
                    }
                    //Orden
                    notes.sort((n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                    callback.onSuccess(notes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo notas de papelera", e);
                    callback.onError("Error al obtener notas de papelera: " + e.getMessage());
                });
    }
    public void saveNote(Note note, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        // CRÍTICO: Validación para prevenir guardados destructivos
        if (note == null) {
            callback.onError("Nota no puede ser null");
            return;
        }

        // VALIDACIÓN CRÍTICA: Si la nota tiene ID pero no tiene etiquetas cargadas,
        // verificar si debería tener etiquetas antes de guardar
        if (note.getId() != null && !note.getId().isEmpty()) {
            List<String> currentTagIds = note.getTagIds();
            if (currentTagIds == null || currentTagIds.isEmpty()) {
                // CRÍTICO: Antes de guardar con listas vacías, verificar si la nota
                // realmente debe tener listas vacías o si simplemente no están cargadas
                Log.w("TAG_DEBUG", String.format("[FIREBASE_SAVE_WARNING] NoteID:%s | Attempting to save with empty tagIds - may cause data loss", note.getId()));

                // Verificar si esta nota tiene etiquetas en Firebase antes de sobrescribir
                getNoteById(note.getId(), new DataCallback<Note>() {
                    @Override
                    public void onSuccess(Note existingNote) {
                        if (existingNote != null && existingNote.getTagIds() != null && !existingNote.getTagIds().isEmpty()) {
                            // La nota existente tiene etiquetas, pero la nota a guardar no
                            // Preservar las etiquetas existentes para evitar pérdida de datos
                            Log.w("TAG_DEBUG", String.format("[FIREBASE_SAVE_PRESERVE] NoteID:%s | Preserving existing tagIds: %d",
                                note.getId(), existingNote.getTagIds().size()));
                            note.setTagIds(existingNote.getTagIds());
                        }
                        // Continuar con el guardado normal
                        performSaveNote(note, callback, userId);
                    }

                    @Override
                    public void onError(String error) {
                        // Si hay error obteniendo la nota existente, continuar con el guardado
                        // pero con advertencia
                        Log.w("TAG_DEBUG", String.format("[FIREBASE_SAVE_FORCE] NoteID:%s | Cannot verify existing tagIds, proceeding with save", note.getId()));
                        performSaveNote(note, callback, userId);
                    }
                });
                return;
            }
        }

        // Si llegamos aquí, es seguro proceder con el guardado
        performSaveNote(note, callback, userId);
    }

    private void performSaveNote(Note note, SimpleCallback callback, String userId) {
        Log.d(TAG, "Guardando nota para usuario: " + userId);
        Log.d(TAG, "Nota tiene " + (note.getTagIds() != null ? note.getTagIds().size() : 0) + " etiquetas");
        note.setTimestamp(System.currentTimeMillis());

        // SOLUCIÓN: Crear el mapa de datos con validación estricta de TagIds
        java.util.Map<String, Object> noteData = new java.util.HashMap<>();
        noteData.put("titulo", note.getTitulo() != null ? note.getTitulo() : "");
        noteData.put("contenido", note.getContenido() != null ? note.getContenido() : "");

        // CRÍTICO: Asegurar que TagIds se serializa correctamente
        List<String> validTagIds = note.getTagIds();
        if (validTagIds == null) {
            validTagIds = new ArrayList<>();
        }
        // Filtrar TagIds nulos o vacíos
        List<String> cleanTagIds = new ArrayList<>();
        for (String tagId : validTagIds) {
            if (tagId != null && !tagId.trim().isEmpty()) {
                cleanTagIds.add(tagId.trim());
            }
        }
        noteData.put("tagIds", cleanTagIds);

        // Log crítico para verificar lo que se está guardando
        Log.w("TAG_DEBUG", String.format("[FIREBASE_SAVE_DATA] NoteID:%s | TagIds to save: %s (Count:%d)",
            note.getId() != null ? note.getId() : "NEW",
            String.join(",", cleanTagIds),
            cleanTagIds.size()));

        noteData.put("isFavorite", note.isFavorite());
        noteData.put("isTrash", note.isTrash());
        noteData.put("timestamp", note.getTimestamp());
        noteData.put("gpsLocation", note.getGpsLocation());

        if (note.getId() == null || note.getId().isEmpty()) {
            Log.d(TAG, "Creando nueva nota en Firestore");
            db.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_NOTES)
                    .add(noteData)
                    .addOnSuccessListener(documentReference -> {
                        note.setId(documentReference.getId());
                        Log.d(TAG, "Nota creada con ID: " + documentReference.getId());
                        Log.w("TAG_DEBUG", String.format("[FIREBASE_SAVE_NEW_SUCCESS] NoteID:%s | TagIds saved: %d",
                            documentReference.getId(), cleanTagIds.size()));
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creando nota", e);
                        Log.e("TAG_DEBUG", "[FIREBASE_SAVE_NEW_ERROR] " + e.getMessage());
                        callback.onError("Error al crear nota: " + e.getMessage());
                    });
        } else {
            Log.d(TAG, "Actualizando nota existente con ID: " + note.getId());
            db.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_NOTES)
                    .document(note.getId())
                    .set(noteData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Nota actualizada");
                        Log.w("TAG_DEBUG", String.format("[FIREBASE_SAVE_UPDATE_SUCCESS] NoteID:%s | TagIds saved: %d",
                            note.getId(), cleanTagIds.size()));
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error actualizando nota", e);
                        Log.e("TAG_DEBUG", "[FIREBASE_SAVE_UPDATE_ERROR] " + note.getId() + " - " + e.getMessage());
                        callback.onError("Error al actualizar nota: " + e.getMessage());
                    });
        }
    }
    public void deleteNotePermanently(String noteId, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || noteId == null || noteId.isEmpty()) {
            callback.onError("Usuario no autenticado o ID de nota inválido");
            return;
        }
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTES)
                .document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Nota eliminada permanentemente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando nota permanentemente", e);
                    callback.onError("Error al eliminar nota: " + e.getMessage());
                });
    }
    public void moveNoteToTrash(String noteId, SimpleCallback callback) {
        updateNoteTrashStatus(noteId, true, callback);
    }
    public void restoreNoteFromTrash(String noteId, SimpleCallback callback) {
        updateNoteTrashStatus(noteId, false, callback);
    }
    private void updateNoteTrashStatus(String noteId, boolean isTrash, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || noteId == null || noteId.isEmpty()) {
            callback.onError("Usuario no autenticado o ID de nota inválido");
            return;
        }
        Log.d(TAG, "Actualizando estado de papelera para nota: " + noteId + " isTrash: " + isTrash);
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTES)
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            note.setId(documentSnapshot.getId());
                            note.setTrash(isTrash);
                            note.setTimestamp(System.currentTimeMillis());
                            // UsarMap
                            java.util.Map<String, Object> noteData = new java.util.HashMap<>();
                            noteData.put("titulo", note.getTitulo());
                            noteData.put("contenido", note.getContenido());
                            noteData.put("tagIds", note.getTagIds() != null ? note.getTagIds() : new ArrayList<>());
                            noteData.put("isFavorite", note.isFavorite());
                            noteData.put("isTrash", isTrash);
                            noteData.put("timestamp", note.getTimestamp());
                            noteData.put("gpsLocation", note.getGpsLocation());

                            db.collection(COLLECTION_USERS)
                                    .document(userId)
                                    .collection(COLLECTION_NOTES)
                                    .document(noteId)
                                    .set(noteData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Estado de papelera actualizado exitosamente");
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error actualizando estado de papelera", e);
                                        callback.onError("Error al actualizar estado: " + e.getMessage());
                                    });
                        } else {
                            callback.onError("Error al obtener datos de la nota");
                        }
                    } else {
                        callback.onError("Nota no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo nota", e);
                    callback.onError("Error al obtener nota: " + e.getMessage());
                });
    }
    public void setNoteFavorite(String noteId, boolean isFavorite, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || noteId == null || noteId.isEmpty()) {
            callback.onError("Usuario no autenticado o ID de nota inválido");
            return;
        }
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTES)
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            note.setId(documentSnapshot.getId());
                            note.setFavorite(isFavorite);
                            java.util.Map<String, Object> noteData = new java.util.HashMap<>();
                            noteData.put("titulo", note.getTitulo());
                            noteData.put("contenido", note.getContenido());
                            noteData.put("tagIds", note.getTagIds() != null ? note.getTagIds() : new ArrayList<>());
                            noteData.put("isFavorite", isFavorite);
                            noteData.put("isTrash", note.isTrash());
                            noteData.put("timestamp", note.getTimestamp());
                            noteData.put("gpsLocation", note.getGpsLocation());

                            db.collection(COLLECTION_USERS)
                                    .document(userId)
                                    .collection(COLLECTION_NOTES)
                                    .document(noteId)
                                    .set(noteData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Nota favorita actualizada");
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error actualizando favorito de nota", e);
                                        callback.onError("Error al actualizar favorito: " + e.getMessage());
                                    });
                        } else {
                            callback.onError("Error al obtener datos de la nota");
                        }
                    } else {
                        callback.onError("Nota no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo nota", e);
                    callback.onError("Error al obtener nota: " + e.getMessage());
                });
    }
    public void getTagsByIds(List<String> tagIds, DataCallback<List<Tag>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        if (tagIds == null || tagIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        Log.d(TAG, "Obteniendo tags por IDs para usuario: " + userId);
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TAGS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Tag> tags = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tag tag = document.toObject(Tag.class);
                        tag.setId(document.getId());
                        if (tagIds.contains(tag.getId())) {
                            tags.add(tag);
                        }
                    }
                    callback.onSuccess(tags);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo tags por IDs", e);
                    callback.onError("Error al obtener etiquetas: " + e.getMessage());
                });
    }
    public void getNoteById(String noteId, DataCallback<Note> callback) {
        String userId = getCurrentUserId();
        if (userId == null || noteId == null || noteId.isEmpty()) {
            callback.onError("Usuario no autenticado o ID de nota inválido");
            return;
        }
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTES)
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            note.setId(documentSnapshot.getId());
                            callback.onSuccess(note);
                        } else {
                            callback.onError("Error al convertir datos de la nota");
                        }
                    } else {
                        callback.onError("Nota no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo nota por ID", e);
                    callback.onError("Error al obtener nota: " + e.getMessage());
                });
    }
}
