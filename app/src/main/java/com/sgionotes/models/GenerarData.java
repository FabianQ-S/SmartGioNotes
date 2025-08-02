package com.sgionotes.models;

import android.content.Context;
import android.content.SharedPreferences;
import com.sgionotes.repository.LocalRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class GenerarData {

    private static GenerarData instancia;
    private LocalRepository localRepository;
    private List<Note> listaNotas;
    private List<Tag> listaEtiquetas;
    private static final String PREFS_NAME = "TagPreferences";
    private static final String FAVORITES_KEY = "favorites";
    private static final String TIMESTAMPS_KEY = "timestamps";
    private static final String TAGS_KEY = "all_tags";
    private static final String TAG_SEPARATOR = "||TAG_SEPARATOR||";

    private GenerarData() {
        listaNotas = new ArrayList<>();
        listaEtiquetas = new ArrayList<>();
    }

    public void initializeWithContext(Context context) {
        if (localRepository == null) {
            localRepository = new LocalRepository(context);
            loadDataFromDatabase();
            createDefaultDataIfEmpty();
        }
    }

    private void loadDataFromDatabase() {
        if (localRepository != null) {
            listaNotas = localRepository.getAllNotes();
            listaEtiquetas = localRepository.getAllTags();
        }
    }

    private void createDefaultDataIfEmpty() {
        if (localRepository == null) return;

        // EtiquetasDefault
        if (listaEtiquetas.isEmpty()) {
            Tag tagTrabajo = new Tag("Trabajo");
            Tag tagPersonal = new Tag("Personal");
            Tag tagImportante = new Tag("Importante");
            Tag tagIdeas = new Tag("Ideas");
            Tag tagUrgente = new Tag("Urgente");

            localRepository.saveTag(tagTrabajo);
            localRepository.saveTag(tagPersonal);
            localRepository.saveTag(tagImportante);
            localRepository.saveTag(tagIdeas);
            localRepository.saveTag(tagUrgente);

            listaEtiquetas = localRepository.getAllTags();
        }

        // NotasDefault
        if (listaNotas.isEmpty()) {
            Tag tagPersonal = getTagByDescription("Personal");
            Tag tagIdeas = getTagByDescription("Ideas");
            Tag tagImportante = getTagByDescription("Importante");
            Tag tagTrabajo = getTagByDescription("Trabajo");
            Tag tagUrgente = getTagByDescription("Urgente");

            Note nota1 = new Note(0,
                    "Lista de compras",
                    "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                    Arrays.asList(tagPersonal),
                    true, false
            );

            Note nota2 = new Note(0,
                    "Idea para proyecto",
                    "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                    Arrays.asList(tagIdeas, tagImportante),
                    true, false
            );

            Note nota3 = new Note(0,
                    "Cita médica",
                    "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                    Arrays.asList(tagPersonal, tagImportante),
                    true, false
            );

            localRepository.saveNote(nota1);
            localRepository.saveNote(nota2);
            localRepository.saveNote(nota3);

            listaNotas = localRepository.getAllNotes();
        }
    }

    private Tag getTagByDescription(String description) {
        for (Tag tag : listaEtiquetas) {
            if (tag.getEtiquetaDescripcion().equals(description)) {
                return tag;
            }
        }
        return new Tag(description);
    }

    public List<Note> getListaNotas() {
        if (localRepository != null) {
            listaNotas = localRepository.getAllNotes();
        }
        return listaNotas;
    }

    public void setListaNotas(List<Note> listaNotas) {
        this.listaNotas = listaNotas;
        // Guardar en SQLite
        if (localRepository != null) {
            for (Note note : listaNotas) {
                localRepository.saveNote(note);
            }
        }
    }

    public List<Tag> getListaEtiquetas() {
        if (localRepository != null) {
            listaEtiquetas = localRepository.getAllTags();
        }
        return listaEtiquetas;
    }

    public void setListaEtiquetas(List<Tag> listaEtiquetas) {
        this.listaEtiquetas = listaEtiquetas;
        // Guardar en SQLite
        if (localRepository != null) {
            for (Tag tag : listaEtiquetas) {
                localRepository.saveTag(tag);
            }
        }
    }

    // Métodos para operaciones directas con SQLite
    public void addNote(Note note) {
        if (localRepository != null) {
            localRepository.saveNote(note);
            listaNotas = localRepository.getAllNotes();
        }
    }

    public void updateNote(Note note) {
        if (localRepository != null) {
            localRepository.saveNote(note);
            listaNotas = localRepository.getAllNotes();
        }
    }

    public void deleteNote(int noteId) {
        if (localRepository != null) {
            localRepository.deleteNote(noteId);
            listaNotas = localRepository.getAllNotes();
        }
    }

    public void moveNoteToTrash(int noteId) {
        if (localRepository != null) {
            localRepository.moveNoteToTrash(noteId);
            listaNotas = localRepository.getAllNotes();
        }
    }

    public void addTag(Tag tag) {
        if (localRepository != null) {
            localRepository.saveTag(tag);
            listaEtiquetas = localRepository.getAllTags();
        }
    }

    public LocalRepository getLocalRepository() {
        return localRepository;
    }

    public static GenerarData getInstance() {
        if (instancia == null) {
            instancia = new GenerarData();
        }
        return instancia;
    }

    public static GenerarData getInstancia() {
        if (instancia == null) {
            instancia = new GenerarData();
        }
        return instancia;
    }

    public void loadFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        
        for (Tag tag : listaEtiquetas) {
            String tagKey = tag.getEtiquetaDescripcion();
            if (favorites.contains(tagKey)) {
                long timestamp = prefs.getLong(TIMESTAMPS_KEY + "_" + tagKey, System.currentTimeMillis());
                tag.setFavorite(true);
                tag.setFavoriteTimestamp(timestamp);
            }
        }
        
        sortTagsByFavorites();
    }

    private void sortTagsByFavorites() {
        listaEtiquetas.sort((tag1, tag2) -> {
            if (tag1.isFavorite() && !tag2.isFavorite()) {
                return -1;
            } else if (!tag1.isFavorite() && tag2.isFavorite()) {
                return 1;
            } else if (tag1.isFavorite() && tag2.isFavorite()) {
                return Long.compare(tag1.getFavoriteTimestamp(), tag2.getFavoriteTimestamp());
            } else {
                return 0;
            }
        });
    }

    public void saveFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> favorites = new HashSet<>();
        for (Tag tag : listaEtiquetas) {
            if (tag.isFavorite()) {
                String tagKey = tag.getEtiquetaDescripcion();
                favorites.add(tagKey);
                editor.putLong(TIMESTAMPS_KEY + "_" + tagKey, tag.getFavoriteTimestamp());
            }
        }

        editor.putStringSet(FAVORITES_KEY, favorites);
        editor.apply();
    }
}
