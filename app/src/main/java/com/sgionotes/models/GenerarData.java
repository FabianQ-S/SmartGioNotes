package com.sgionotes.models;
import android.content.Context;
import android.util.Log;
import com.sgionotes.repository.FirestoreRepository;
import java.util.ArrayList;
import java.util.List;
public class GenerarData {
    public interface DataChangeListener {
        void onDataChanged();
    }
    public interface DataInitializationCallback {
        void onInitializationComplete();
        void onInitializationError(String error);
    }
    private static GenerarData instancia;
    private FirestoreRepository firestoreRepository;
    private List<Note> listaNotas;
    private List<Tag> listaEtiquetas;
    private List<DataChangeListener> dataChangeListeners;

    private GenerarData() {
        listaNotas = new ArrayList<>();
        listaEtiquetas = new ArrayList<>();
        dataChangeListeners = new ArrayList<>();
    }
    public static GenerarData getInstancia() {
        if (instancia == null) {
            instancia = new GenerarData();
        }
        return instancia;
    }
    public static GenerarData getInstance() {
        return getInstancia();
    }
    //Metodos
    public void addDataChangeListener(DataChangeListener listener) {
        if (!dataChangeListeners.contains(listener)) {
            dataChangeListeners.add(listener);
        }
    }
    public void removeDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.remove(listener);
    }
    private void notifyDataChanged() {
        Log.d("GenerarData", "Notificando cambio de datos a " + dataChangeListeners.size() + " listeners - Notas: " + getListaNotas().size() + ", Etiquetas: " + getListaEtiquetas().size());
        for (DataChangeListener listener : dataChangeListeners) {
            listener.onDataChanged();
        }
    }

    // Método público para notificar cambios desde otras clases
    public void forceNotifyDataChanged() {
        notifyDataChanged();
    }
    public void initializeWithContext(Context context) {
        if (firestoreRepository == null) {
            firestoreRepository = new FirestoreRepository(context);
        }
        loadDataFromFirestore(); //CargarDatos
    }
    private void loadDataFromFirestore() {
        if (firestoreRepository == null) return;
        String currentUserId = firestoreRepository.getCurrentUserId();
        Log.d("GenerarData", "Cargando datos para usuario: " + currentUserId);
        //IncluirNotasTotales
        firestoreRepository.getAllNotesIncludingTrash(new FirestoreRepository.DataCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                listaNotas = notes;
                Log.d("GenerarData", "Notas cargadas exitosamente: " + notes.size() + " notas para usuario " + currentUserId);
                notifyDataChanged();
            }
            @Override
            public void onError(String error) {
                listaNotas = new ArrayList<>();
                Log.e("GenerarData", "Error cargando notas: " + error);
                notifyDataChanged();
            }
        });
        firestoreRepository.getAllTags(new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> tags) {
                listaEtiquetas = tags;
                Log.d("GenerarData", "Etiquetas cargadas exitosamente: " + tags.size() + " etiquetas para usuario " + currentUserId);
                notifyDataChanged();
            }
            @Override
            public void onError(String error) {
                listaEtiquetas = new ArrayList<>();
                Log.e("GenerarData", "Error cargando etiquetas: " + error);
                notifyDataChanged();
            }
        });
    }
    public FirestoreRepository getFirestoreRepository() {
        return firestoreRepository;
    }

    public List<Note> getListaNotas() {
        if (listaNotas == null) {
            listaNotas = new ArrayList<>();
        }
        return listaNotas;
    }
    public List<Tag> getListaEtiquetas() {
        if (listaEtiquetas == null) {
            listaEtiquetas = new ArrayList<>();
        }
        return listaEtiquetas;
    }
    public void addNota(Note nota) {
        if (listaNotas == null) {
            listaNotas = new ArrayList<>();
        }
        listaNotas.add(nota);
        notifyDataChanged();
        if (firestoreRepository != null) {
            firestoreRepository.saveNote(nota, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Log.d("GenerarData", "Nota guardada exitosamente en Firestore");
                }
                @Override
                public void onError(String error) {
                    Log.e("GenerarData", "Error guardando nota: " + error);
                }
            });
        }
    }
    public void addTag(Tag tag) {
        if (listaEtiquetas == null) {
            listaEtiquetas = new ArrayList<>();
        }
        listaEtiquetas.add(tag);
        notifyDataChanged();
        if (firestoreRepository != null) {
            firestoreRepository.saveTag(tag, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Log.d("GenerarData", "Etiqueta guardada exitosamente en Firestore");
                }
                @Override
                public void onError(String error) {
                    Log.e("GenerarData", "Error guardando etiqueta: " + error);
                }
            });
        }
    }
    public void refreshData() {
        Log.d("GenerarData", "Refrescando datos inmediatamente");
        if (firestoreRepository != null) {
            String currentUserId = firestoreRepository.getCurrentUserId();
            if (currentUserId != null) {
                loadDataFromFirestore();
            }
        }
    }
    // Método para actualizar una nota específica en la lista local
    public void updateNoteInLocalList(Note updatedNote) {
        if (listaNotas != null && updatedNote != null && updatedNote.getId() != null) {
            for (int i = 0; i < listaNotas.size(); i++) {
                Note note = listaNotas.get(i);
                if (note.getId().equals(updatedNote.getId())) {
                    listaNotas.set(i, updatedNote);
                    Log.d("GenerarData", "Nota actualizada en lista local: " + updatedNote.getId());
                    notifyDataChanged();
                    return;
                }
            }
            // Si no se encontró, puede ser una nota nueva
            if (!updatedNote.isTrash()) {
                listaNotas.add(updatedNote);
                Log.d("GenerarData", "Nueva nota agregada a lista local: " + updatedNote.getId());
                notifyDataChanged();
            }
        }
    }
    // Método para limpiar referencias a etiquetas eliminadas
    public void cleanupDeletedTagReferences(String deletedTagId) {
        if (listaNotas != null && deletedTagId != null) {
            boolean hasChanges = false;
            for (Note note : listaNotas) {
                if (note.getTagIds() != null && note.getTagIds().contains(deletedTagId)) {
                    List<String> newTagIds = new ArrayList<>(note.getTagIds());
                    newTagIds.remove(deletedTagId);
                    note.setTagIds(newTagIds);
                    hasChanges = true;
                    Log.d("GenerarData", "Eliminada referencia a etiqueta " + deletedTagId + " de nota " + note.getId());
                }
            }
            if (hasChanges) {
                notifyDataChanged();
            }
        }
    }

    public void clearUserData() {
        if (listaNotas != null) {
            listaNotas.clear();
        }
        if (listaEtiquetas != null) {
            listaEtiquetas.clear();
        }
        notifyDataChanged();
    }
    public void forceReloadUserData() {
        clearUserData();
        loadDataFromFirestore();
    }
    public void refreshDataForCurrentUser() {
        refreshDataForCurrentUser(null);
    }
    public void refreshDataForCurrentUser(DataInitializationCallback callback) {
        String currentUserId = firestoreRepository != null ? firestoreRepository.getCurrentUserId() : null;
        if (currentUserId != null) {
            Log.d("GenerarData", "Refrescando datos para usuario actual: " + currentUserId);
            clearUserData();
            loadDataFromFirestoreWithCallback(callback);
        } else {
            Log.w("GenerarData", "No hay usuario autenticado para refrescar datos");
            if (callback != null) {
                callback.onInitializationError("No hay usuario autenticado");
            }
        }
    }
    private void loadDataFromFirestoreWithCallback(DataInitializationCallback callback) {
        if (firestoreRepository == null) {
            if (callback != null) callback.onInitializationError("Repository not initialized");
            return;
        }
        String currentUserId = firestoreRepository.getCurrentUserId();
        Log.d("GenerarData", "Cargando datos con callback para usuario: " + currentUserId);
        final boolean[] notesLoaded = {false};
        final boolean[] tagsLoaded = {false};

        firestoreRepository.getAllNotesIncludingTrash(new FirestoreRepository.DataCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                listaNotas = notes;
                notesLoaded[0] = true;
                Log.d("GenerarData", "Notas cargadas con callback: " + notes.size() + " notas para usuario " + currentUserId);
                notifyDataChanged();
                checkCallbackCompletion(callback, notesLoaded, tagsLoaded);
            }
            @Override
            public void onError(String error) {
                listaNotas = new ArrayList<>();
                notesLoaded[0] = true;
                Log.e("GenerarData", "Error cargando notas con callback: " + error);
                notifyDataChanged();
                checkCallbackCompletion(callback, notesLoaded, tagsLoaded);
            }
        });
        firestoreRepository.getAllTags(new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> tags) {
                listaEtiquetas = tags;
                tagsLoaded[0] = true;
                Log.d("GenerarData", "Etiquetas cargadas con callback: " + tags.size() + " etiquetas para usuario " + currentUserId);
                notifyDataChanged();
                checkCallbackCompletion(callback, notesLoaded, tagsLoaded);
            }
            @Override
            public void onError(String error) {
                listaEtiquetas = new ArrayList<>();
                tagsLoaded[0] = true;
                Log.e("GenerarData", "Error cargando etiquetas con callback: " + error);
                notifyDataChanged();
                checkCallbackCompletion(callback, notesLoaded, tagsLoaded);
            }
        });
    }
    private void checkCallbackCompletion(DataInitializationCallback callback, boolean[] notesLoaded, boolean[] tagsLoaded) {
        if (callback != null && notesLoaded[0] && tagsLoaded[0]) {
            Log.d("GenerarData", "Datos completamente cargados - ejecutando callback");
            notifyDataChanged();
            callback.onInitializationComplete();
        }
    }
    public void forceUpdateAllFragments() {
        Log.d("GenerarData", "Forzando actualización de todos los fragmentos");
        notifyDataChanged();
    }

    public boolean hasDataLoaded() {
        return (listaNotas != null && !listaNotas.isEmpty()) ||
               (listaEtiquetas != null && !listaEtiquetas.isEmpty());
    }

    public void ensureDataLoaded(DataInitializationCallback callback) {
        String currentUserId = firestoreRepository != null ? firestoreRepository.getCurrentUserId() : null;
        if (currentUserId == null) {
            if (callback != null) {
                callback.onInitializationError("No hay usuario autenticado");
            }
            return;
        }

        // Si ya hay datos cargados, ejecutar callback inmediatamente
        if (hasDataLoaded()) {
            if (callback != null) {
                callback.onInitializationComplete();
            }
            return;
        }

        // Si no hay datos, cargar desde Firestore
        loadDataFromFirestoreWithCallback(callback);
    }

    // Método seguro para eliminar etiquetas
    public void removeTag(String tagId, FirestoreRepository.SimpleCallback callback) {
        if (tagId == null || listaEtiquetas == null) {
            if (callback != null) callback.onError("ID de etiqueta inválido");
            return;
        }

        // Primero limpiar referencias en notas locales
        cleanupDeletedTagReferences(tagId);

        // Eliminar de la lista local
        listaEtiquetas.removeIf(tag -> tagId.equals(tag.getId()));

        // Notificar cambios inmediatamente
        notifyDataChanged();

        // Eliminar de Firestore
        if (firestoreRepository != null) {
            firestoreRepository.deleteTag(tagId, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Log.d("GenerarData", "Etiqueta eliminada exitosamente de Firestore: " + tagId);
                    // Actualizar notas en Firestore que tenían esta etiqueta
                    updateNotesAfterTagDeletion(tagId);
                    if (callback != null) callback.onSuccess();
                }

                @Override
                public void onError(String error) {
                    Log.e("GenerarData", "Error eliminando etiqueta de Firestore: " + error);
                    // Recargar datos para mantener consistencia
                    refreshData();
                    if (callback != null) callback.onError(error);
                }
            });
        } else {
            if (callback != null) callback.onError("Repository no inicializado");
        }
    }

    // Método para actualizar notas en Firestore después de eliminar una etiqueta
    private void updateNotesAfterTagDeletion(String deletedTagId) {
        if (listaNotas == null || firestoreRepository == null) return;

        for (Note note : listaNotas) {
            if (note.getTagIds() != null && note.getTagIds().contains(deletedTagId)) {
                // Crear una copia de la nota con la etiqueta eliminada
                Note updatedNote = new Note(note);
                List<String> newTagIds = new ArrayList<>(note.getTagIds());
                newTagIds.remove(deletedTagId);
                updatedNote.setTagIds(newTagIds);

                // Actualizar en Firestore
                firestoreRepository.saveNote(updatedNote, new FirestoreRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("GenerarData", "Nota actualizada después de eliminar etiqueta: " + note.getId());
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("GenerarData", "Error actualizando nota después de eliminar etiqueta: " + error);
                    }
                });
            }
        }
    }

    // Método para obtener el número de etiquetas válidas de una nota
    public int getValidTagCountForNote(Note note) {
        if (note == null || note.getTagIds() == null || listaEtiquetas == null) {
            return 0;
        }

        int validCount = 0;
        for (String tagId : note.getTagIds()) {
            // Verificar que la etiqueta aún existe
            boolean tagExists = false;
            for (Tag tag : listaEtiquetas) {
                if (tag.getId() != null && tag.getId().equals(tagId)) {
                    tagExists = true;
                    break;
                }
            }
            if (tagExists) {
                validCount++;
            }
        }
        return validCount;
    }

    // Método para sincronizar inmediatamente después de crear una nota
    public void addNotaWithImmediateSync(Note nota, FirestoreRepository.SimpleCallback callback) {
        if (listaNotas == null) {
            listaNotas = new ArrayList<>();
        }

        // Agregar a la lista local primero
        listaNotas.add(nota);
        notifyDataChanged();

        if (firestoreRepository != null) {
            firestoreRepository.saveNote(nota, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Log.d("GenerarData", "Nota guardada exitosamente en Firestore con sync inmediato");
                    // Recargar datos para asegurar sincronización
                    refreshData();
                    if (callback != null) callback.onSuccess();
                }

                @Override
                public void onError(String error) {
                    Log.e("GenerarData", "Error guardando nota: " + error);
                    // Remover de lista local si falló
                    listaNotas.remove(nota);
                    notifyDataChanged();
                    if (callback != null) callback.onError(error);
                }
            });
        } else {
            if (callback != null) callback.onError("Repository no inicializado");
        }
    }

    // Método para forzar reinicialización completa del sistema
    public void forceCompleteReinitialization(Context context) {
        Log.d("GenerarData", "Forzando reinicialización completa del sistema");

        // Limpiar datos actuales
        clearUserData();

        // Reinicializar repository si es necesario
        if (firestoreRepository == null) {
            firestoreRepository = new FirestoreRepository(context);
        }

        // Verificar si el usuario cambió
        String currentUserId = firestoreRepository.getCurrentUserId();
        String previousUserId = firestoreRepository.getPreviousUserId();

        if (currentUserId != null && !currentUserId.equals(previousUserId)) {
            Log.d("GenerarData", "Usuario detectado como cambiado en getCurrentUserId(): " + previousUserId + " -> " + currentUserId);
            Log.d("GenerarData", "Reinicializando para usuario: " + currentUserId);
        }

        // Cargar datos del usuario actual
        loadDataFromFirestore();

        // Notificar cambios
        notifyDataChanged();
    }
}
