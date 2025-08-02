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
import java.util.ArrayList;
import java.util.List;

public class FirestoreRepository {
    private static final String TAG = "FirestoreRepository";
    private static final String COLLECTION_NOTES = "notes";
    private static final String COLLECTION_TAGS = "tags";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private Context context;
    public FirestoreRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        mAuth = FirebaseAuth.getInstance();
        this.context = context;
    }
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    public void getAllTags(DataCallback<List<Tag>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        db.collection(COLLECTION_TAGS)
                .whereEqualTo("userId", userId)
                .orderBy("isFavorite", Query.Direction.DESCENDING)
                .orderBy("favoriteTimestamp", Query.Direction.ASCENDING)
                .orderBy("etiquetaDescripcion", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Tag> tags = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tag tag = document.toObject(Tag.class);
                        tag.setId(document.getId());
                        tags.add(tag);
                    }
                    callback.onSuccess(tags);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo tags", e);
                    callback.onError("Error al obtener etiquetas: " + e.getMessage());
                });
    }
    public void getFavoriteTags(DataCallback<List<Tag>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        db.collection(COLLECTION_TAGS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isFavorite", true)
                .orderBy("favoriteTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Tag> tags = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tag tag = document.toObject(Tag.class);
                        tag.setId(document.getId());
                        tags.add(tag);
                    }
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
        Log.d(TAG, "Intentando guardar tag: " + tag.getEtiquetaDescripcion() + " para usuario: " + userId);
        tag.setUserId(userId);
        if (tag.getId() == null || tag.getId().isEmpty()) {

            //NuevaEtiqueta
            Log.d(TAG, "Creando nueva etiqueta en Firestore");
            db.collection(COLLECTION_TAGS)
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
            //ActualizarEtiqueta
            Log.d(TAG, "Actualizando etiqueta existente con ID: " + tag.getId());
            db.collection(COLLECTION_TAGS)
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
        if (tagId == null || tagId.isEmpty()) {
            callback.onError("ID de etiqueta inválido");
            return;
        }
        db.collection(COLLECTION_TAGS)
                .document(tagId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tag eliminado");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando tag", e);
                    callback.onError("Error al eliminar etiqueta: " + e.getMessage());
                });
    }
    public void setTagFavorite(String tagId, boolean isFavorite, SimpleCallback callback) {
        if (tagId == null || tagId.isEmpty()) {
            callback.onError("ID de etiqueta inválido");
            return;
        }
        db.collection(COLLECTION_TAGS)
                .document(tagId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Tag tag = documentSnapshot.toObject(Tag.class);
                        if (tag != null) {
                            tag.setId(documentSnapshot.getId());
                            tag.setFavorite(isFavorite);

                            db.collection(COLLECTION_TAGS)
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
    //Notas
    public void getAllNotes(DataCallback<List<Note>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        db.collection(COLLECTION_NOTES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isTrash", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Note> notes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Note note = document.toObject(Note.class);
                        note.setId(document.getId());
                        notes.add(note);
                    }
                    callback.onSuccess(notes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo notas", e);
                    callback.onError("Error al obtener notas: " + e.getMessage());
                });
    }
    public void getTrashNotes(DataCallback<List<Note>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        db.collection(COLLECTION_NOTES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isTrash", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Note> notes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Note note = document.toObject(Note.class);
                        note.setId(document.getId());
                        notes.add(note);
                    }
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
        note.setUserId(userId);
        note.setTimestamp(System.currentTimeMillis());
        if (note.getId() == null || note.getId().isEmpty()) {
            //CrearNota
            db.collection(COLLECTION_NOTES)
                    .add(note)
                    .addOnSuccessListener(documentReference -> {
                        note.setId(documentReference.getId());
                        Log.d(TAG, "Nota creada con ID: " + documentReference.getId());
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creando nota", e);
                        callback.onError("Error al crear nota: " + e.getMessage());
                    });
        } else {
            //ActualizarNota
            db.collection(COLLECTION_NOTES)
                    .document(note.getId())
                    .set(note)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Nota actualizada");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error actualizando nota", e);
                        callback.onError("Error al actualizar nota: " + e.getMessage());
                    });
        }
    }
    public void deleteNotePermanently(String noteId, SimpleCallback callback) {
        if (noteId == null || noteId.isEmpty()) {
            callback.onError("ID de nota inválido");
            return;
        }
        db.collection(COLLECTION_NOTES)
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
        if (noteId == null || noteId.isEmpty()) {
            callback.onError("ID de nota inválido");
            return;
        }
        db.collection(COLLECTION_NOTES)
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            note.setId(documentSnapshot.getId());
                            note.setTrash(isTrash);
                            note.setTimestamp(System.currentTimeMillis());

                            db.collection(COLLECTION_NOTES)
                                    .document(noteId)
                                    .set(note)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Estado de papelera actualizado");
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
        if (noteId == null || noteId.isEmpty()) {
            callback.onError("ID de nota inválido");
            return;
        }
        db.collection(COLLECTION_NOTES)
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            note.setId(documentSnapshot.getId());
                            note.setFavorite(isFavorite);

                            db.collection(COLLECTION_NOTES)
                                    .document(noteId)
                                    .set(note)
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
}
