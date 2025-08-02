package com.sgionotes.repository;

import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sgionotes.models.Note;
import com.sgionotes.models.Tag;
import com.sgionotes.models.GenerarData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreRepository {

    private static final String TAG = "FirestoreRepository";
    private static final String COLLECTION_NOTES = "notes";
    private static final String COLLECTION_TAGS = "tags";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;

    public FirestoreRepository(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public interface DataSyncCallback {
        void onSuccess();
        void onError(String error);
    }

    // SincronizarDatosRed
    public void backupLocalDataToFirestore(DataSyncCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        GenerarData generarData = GenerarData.getInstancia();
        LocalRepository localRepo = generarData.getLocalRepository();

        if (localRepo == null) {
            callback.onError("Repository local no inicializado");
            return;
        }

        String userId = user.getUid();

        // BackupEtiquetas
        backupTagsToFirestore(userId, localRepo, new DataSyncCallback() {
            @Override
            public void onSuccess() {
                // Luego hacer backup de las notas
                backupNotesToFirestore(userId, localRepo, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError("Error al respaldar etiquetas: " + error);
            }
        });
    }

    // RestaurarDatos
    public void restoreDataFromFirestore(DataSyncCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String userId = user.getUid();

        // RestaurarEtiquetas
        restoreTagsFromFirestore(userId, new DataSyncCallback() {
            @Override
            public void onSuccess() {
                restoreNotesFromFirestore(userId, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError("Error al restaurar etiquetas: " + error);
            }
        });
    }

    private void backupTagsToFirestore(String userId, LocalRepository localRepo, DataSyncCallback callback) {
        List<Tag> tags = localRepo.getAllTags();

        // EliminarEtiquetasExistentes
        db.collection("users").document(userId).collection(COLLECTION_TAGS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Eliminar etiquetas existentes
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        for (Tag tag : tags) {
                            Map<String, Object> tagData = tagToMap(tag);
                            db.collection("users").document(userId).collection(COLLECTION_TAGS)
                                    .add(tagData);
                        }
                        Log.d(TAG, "Etiquetas respaldadas en Firestore: " + tags.size());
                        callback.onSuccess();
                    } else {
                        Log.w(TAG, "Error al respaldar etiquetas", task.getException());
                        callback.onError("Error al respaldar etiquetas");
                    }
                });
    }

    private void backupNotesToFirestore(String userId, LocalRepository localRepo, DataSyncCallback callback) {
        List<Note> notes = localRepo.getAllNotes();
        List<Note> trashNotes = localRepo.getTrashNotes();

        // ConvinarNotas
        List<Note> allNotes = new ArrayList<>(notes);
        allNotes.addAll(trashNotes);

        // EliminarNotasExistentes
        db.collection("users").document(userId).collection(COLLECTION_NOTES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // EliminarNotasExistentes
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }

                        // SubirNotasActuales
                        for (Note note : allNotes) {
                            Map<String, Object> noteData = noteToMap(note);
                            db.collection("users").document(userId).collection(COLLECTION_NOTES)
                                    .add(noteData);
                        }

                        Log.d(TAG, "Notas respaldadas en Firestore: " + allNotes.size());
                        callback.onSuccess();
                    } else {
                        Log.w(TAG, "Error al respaldar notas", task.getException());
                        callback.onError("Error al respaldar notas");
                    }
                });
    }

    private void restoreTagsFromFirestore(String userId, DataSyncCallback callback) {
        GenerarData generarData = GenerarData.getInstancia();
        LocalRepository localRepo = generarData.getLocalRepository();

        if (localRepo == null) {
            callback.onError("Repository local no inicializado");
            return;
        }

        db.collection("users").document(userId).collection(COLLECTION_TAGS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Tag> tags = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Tag tag = documentToTag(document);
                            if (tag != null) {
                                tags.add(tag);
                                localRepo.saveTag(tag); //Sqlite
                            }
                        }

                        Log.d(TAG, "Etiquetas restauradas desde Firestore: " + tags.size());
                        callback.onSuccess();
                    } else {
                        Log.w(TAG, "Error al restaurar etiquetas", task.getException());
                        callback.onError("Error al restaurar etiquetas");
                    }
                });
    }

    private void restoreNotesFromFirestore(String userId, DataSyncCallback callback) {
        GenerarData generarData = GenerarData.getInstancia();
        LocalRepository localRepo = generarData.getLocalRepository();

        if (localRepo == null) {
            callback.onError("Repository local no inicializado");
            return;
        }

        db.collection("users").document(userId).collection(COLLECTION_NOTES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Note> notes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Note note = documentToNote(document);
                            if (note != null) {
                                notes.add(note);
                                localRepo.saveNote(note); //Sqlite
                            }
                        }

                        Log.d(TAG, "Notas restauradas desde Firestore: " + notes.size());
                        callback.onSuccess();
                    } else {
                        Log.w(TAG, "Error al restaurar notas", task.getException());
                        callback.onError("Error al restaurar notas");
                    }
                });
    }

    // ConvertirNoteMapParaFirestore
    private Map<String, Object> noteToMap(Note note) {
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("id", note.getId());
        noteData.put("titulo", note.getTitulo());
        noteData.put("contenido", note.getContenido());
        noteData.put("isTrash", note.isTrash());

        // ConvertirTagsListaDeStrings
        List<String> tagNames = new ArrayList<>();
        if (note.getEtiquetas() != null) {
            for (Tag tag : note.getEtiquetas()) {
                tagNames.add(tag.getEtiquetaDescripcion());
            }
        }
        noteData.put("tags", tagNames);

        return noteData;
    }

    // ConvertirMapFirestoreNote
    private Note documentToNote(QueryDocumentSnapshot document) {
        try {
            Long id = document.getLong("id");
            String titulo = document.getString("titulo");
            String contenido = document.getString("contenido");
            Boolean isTrash = document.getBoolean("isTrash");
            List<String> tagNames = (List<String>) document.get("tags");

            // CrearListaDeTags
            List<Tag> tags = new ArrayList<>();
            if (tagNames != null) {
                for (String tagName : tagNames) {
                    tags.add(new Tag(tagName));
                }
            }

            return new Note(
                id != null ? id.intValue() : 0,
                titulo != null ? titulo : "",
                contenido != null ? contenido : "",
                tags,
                false,
                isTrash != null ? isTrash : false
            );
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir documento a Note", e);
            return null;
        }
    }

    //TagMap
    private Map<String, Object> tagToMap(Tag tag) {
        Map<String, Object> tagData = new HashMap<>();
        tagData.put("descripcion", tag.getEtiquetaDescripcion());
        tagData.put("isFavorite", tag.isFavorite());
        tagData.put("favoriteTimestamp", tag.getFavoriteTimestamp());
        return tagData;
    }

    // FirestoreTagDesdeUnDocumento
    private Tag documentToTag(QueryDocumentSnapshot document) {
        try {
            String descripcion = document.getString("descripcion");
            Boolean isFavorite = document.getBoolean("isFavorite");
            Long favoriteTimestamp = document.getLong("favoriteTimestamp");

            Tag tag = new Tag(descripcion != null ? descripcion : "");
            tag.setFavorite(isFavorite != null ? isFavorite : false);
            tag.setFavoriteTimestamp(favoriteTimestamp != null ? favoriteTimestamp : System.currentTimeMillis());

            return tag;
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir documento a Tag", e);
            return null;
        }
    }
}
