package com.sgionotes.models;
import android.content.Context;
import android.util.Log;
import com.sgionotes.repository.FirestoreRepository;
import java.util.ArrayList;
import java.util.List;
public class GenerarData {

    public interface DataInitializationCallback {
        void onInitializationComplete();
        void onInitializationError(String error);
    }

    private static GenerarData instancia;
    private FirestoreRepository firestoreRepository;
    private List<Note> listaNotas;
    private List<Tag> listaEtiquetas;
    private GenerarData() {
        listaNotas = new ArrayList<>();
        listaEtiquetas = new ArrayList<>();
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

    public void initializeWithContext(Context context) {
        if (firestoreRepository == null) {
            firestoreRepository = new FirestoreRepository(context);
            loadDataFromFirestore();
        }
    }
    private void loadDataFromFirestore() {
        if (firestoreRepository == null) return;
        firestoreRepository.getAllNotes(new FirestoreRepository.DataCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                listaNotas = notes;
            }

            @Override
            public void onError(String error) {
                listaNotas = new ArrayList<>();
            }
        });
        firestoreRepository.getAllTags(new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> tags) {
                listaEtiquetas = tags;
            }

            @Override
            public void onError(String error) {
                listaEtiquetas = new ArrayList<>();
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
        if (firestoreRepository != null) {
            firestoreRepository.saveNote(nota, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onError(String error) {}
            });
        }
    }
    public void addTag(Tag tag) {
        if (listaEtiquetas == null) {
            listaEtiquetas = new ArrayList<>();
        }
        listaEtiquetas.add(tag);
        if (firestoreRepository != null) {
            firestoreRepository.saveTag(tag, new FirestoreRepository.SimpleCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onError(String error) {}
            });
        }
    }
    public void loadFavorites(Context context) {
        if (firestoreRepository != null) {
            firestoreRepository.getAllTags(new FirestoreRepository.DataCallback<List<Tag>>() {
                @Override
                public void onSuccess(List<Tag> tags) {
                    listaEtiquetas = tags;
                }

                @Override
                public void onError(String error) {
                }
            });
        }
    }
    public void saveFavorites(Context context) {
    }
    public void createDefaultDataIfEmpty() {
        createDefaultDataIfEmptyWithCallback(null);
    }
    public void createDefaultDataIfEmptyWithCallback(DataInitializationCallback callback) {
        if (firestoreRepository == null) {
            if (callback != null) callback.onInitializationError("Repository not initialized");
            return;
        }
        String currentUserId = firestoreRepository.getCurrentUserId();
        if (currentUserId == null) {
            if (callback != null) callback.onInitializationError("Usuario no autenticado");
            return;
        }
        Log.d("GenerarData", "Usuario autenticado: " + currentUserId + " - Iniciando con lienzo en blanco");
        if (listaEtiquetas == null) {
            listaEtiquetas = new ArrayList<>();
        }
        if (listaNotas == null) {
            listaNotas = new ArrayList<>();
        }
        final boolean[] tagsLoaded = {false};
        final boolean[] notesLoaded = {false};
        //CargarEtiquetas
        firestoreRepository.getAllTags(new FirestoreRepository.DataCallback<List<Tag>>() {
            @Override
            public void onSuccess(List<Tag> tags) {
                listaEtiquetas = tags;
                tagsLoaded[0] = true;
                Log.d("GenerarData", "Etiquetas cargadas para usuario " + currentUserId + ": " + tags.size());
                checkLoadingComplete(callback, tagsLoaded, notesLoaded);
            }
            @Override
            public void onError(String error) {
                Log.e("GenerarData", "Error cargando etiquetas: " + error);
                listaEtiquetas = new ArrayList<>();
                tagsLoaded[0] = true;
                checkLoadingComplete(callback, tagsLoaded, notesLoaded);
            }
        });
        //CargarNotas
        firestoreRepository.getAllNotes(new FirestoreRepository.DataCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                listaNotas = notes;
                notesLoaded[0] = true;
                Log.d("GenerarData", "Notas cargadas para usuario " + currentUserId + ": " + notes.size());
                checkLoadingComplete(callback, tagsLoaded, notesLoaded);
            }
            @Override
            public void onError(String error) {
                Log.e("GenerarData", "Error cargando notas: " + error);
                listaNotas = new ArrayList<>();
                notesLoaded[0] = true;
                checkLoadingComplete(callback, tagsLoaded, notesLoaded);
            }
        });
    }
    private void checkLoadingComplete(DataInitializationCallback callback, boolean[] tagsLoaded, boolean[] notesLoaded) {
        if (callback != null && tagsLoaded[0] && notesLoaded[0]) {
            callback.onInitializationComplete();
        }
    }
}
