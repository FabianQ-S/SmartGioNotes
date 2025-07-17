package com.sgionotes.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerarData {

    private static GenerarData instancia;
    private List<Note> listaNotas;
    private List<Tag> listaEtiquetas;

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

}
