package com.sgionotes.models;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class GenerarData {

    private static GenerarData instancia;
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

        Tag tagTrabajo = new Tag("Trabajo");
        Tag tagPersonal = new Tag("Personal");
        Tag tagImportante = new Tag("Importante");
        Tag tagIdeas = new Tag("Ideas");
        Tag tagUrgente = new Tag("Urgente");

        listaEtiquetas.add(tagTrabajo);
        listaEtiquetas.add(tagPersonal);
        listaEtiquetas.add(tagImportante);
        listaEtiquetas.add(tagIdeas);
        listaEtiquetas.add(tagUrgente);

        listaNotas.add(new Note(1,
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(tagPersonal),
                true, false
        ));

        listaNotas.add(new Note(2,
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(tagIdeas, tagImportante),
                true, false
        ));

        listaNotas.add(new Note(3,
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(tagPersonal, tagImportante),
                true, false
        ));

        listaNotas.add(new Note(4,
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(tagTrabajo),
                true, false
        ));

        listaNotas.add(new Note(5,
                "Reunión con cliente",
                "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                Arrays.asList(tagTrabajo, tagUrgente),
                true, false
        ));

        listaNotas.add(new Note(6,
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(tagPersonal),
                true, true
        ));
        listaNotas.add(new Note(7,
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(tagIdeas, tagImportante),
                true, false
        ));

        listaNotas.add(new Note(8,
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(tagPersonal, tagImportante),
                true, true
        ));

        listaNotas.add(new Note(9,
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(tagTrabajo),
                true, true
        ));

    }

    public List<Note> getListaNotas() {
        return listaNotas;
    }

    public void setListaNotas(List<Note> listaNotas) {
        this.listaNotas = listaNotas;
    }

    public List<Tag> getListaEtiquetas() {
        return listaEtiquetas;
    }

    public void setListaEtiquetas(List<Tag> listaEtiquetas) {
        this.listaEtiquetas = listaEtiquetas;
    }

    public static GenerarData getInstance() {
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
