package com.sgionotes.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerarData {

    private List<Note> listaNotas;

    public GenerarData() {
        listaNotas = new ArrayList<>();

        Etiqueta etiquetaTrabajo = new Etiqueta("Trabajo");
        Etiqueta etiquetaPersonal = new Etiqueta("Personal");
        Etiqueta etiquetaImportante = new Etiqueta("Importante");
        Etiqueta etiquetaIdeas = new Etiqueta("Ideas");
        Etiqueta etiquetaUrgente = new Etiqueta("Urgente");

        listaNotas.add(new Note(
                "Reunión con cliente",
                "No olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.No olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\n",
                Arrays.asList(etiquetaTrabajo, etiquetaUrgente, etiquetaIdeas),
                true
        ));


        listaNotas.add(new Note(
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(etiquetaPersonal),
                true
        ));


        listaNotas.add(new Note(
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(etiquetaIdeas, etiquetaImportante),
                true
        ));

        listaNotas.add(new Note(
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(etiquetaPersonal, etiquetaImportante),
                false
        ));

        listaNotas.add(new Note(
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(etiquetaTrabajo),
                true
        ));

        listaNotas.add(new Note(
                "Reunión con cliente",
                "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                Arrays.asList(etiquetaTrabajo, etiquetaUrgente),
                false
        ));


        listaNotas.add(new Note(
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(etiquetaPersonal),
                true
        ));


        listaNotas.add(new Note(
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(etiquetaIdeas, etiquetaImportante),
                true
        ));

        listaNotas.add(new Note(
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(etiquetaPersonal, etiquetaImportante),
                false
        ));

        listaNotas.add(new Note(
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(etiquetaTrabajo),
                true
        ));

        listaNotas.add(new Note(
                "Reunión con cliente",
                "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                Arrays.asList(etiquetaTrabajo, etiquetaUrgente),
                false
        ));


        listaNotas.add(new Note(
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(etiquetaPersonal),
                true
        ));


        listaNotas.add(new Note(
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(etiquetaIdeas, etiquetaImportante),
                true
        ));

        listaNotas.add(new Note(
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(etiquetaPersonal, etiquetaImportante),
                false
        ));

        listaNotas.add(new Note(
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(etiquetaTrabajo),
                true
        ));

        listaNotas.add(new Note(
                "Reunión con cliente",
                "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                Arrays.asList(etiquetaTrabajo, etiquetaUrgente),
                false
        ));


        listaNotas.add(new Note(
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(etiquetaPersonal),
                true
        ));


        listaNotas.add(new Note(
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(etiquetaIdeas, etiquetaImportante),
                true
        ));

        listaNotas.add(new Note(
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(etiquetaPersonal, etiquetaImportante),
                false
        ));

        listaNotas.add(new Note(
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(etiquetaTrabajo),
                true
        ));

        listaNotas.add(new Note(
                "Reunión con cliente",
                "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                Arrays.asList(etiquetaTrabajo, etiquetaUrgente),
                false
        ));


        listaNotas.add(new Note(
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(etiquetaPersonal),
                true
        ));


        listaNotas.add(new Note(
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(etiquetaIdeas, etiquetaImportante),
                true
        ));

        listaNotas.add(new Note(
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(etiquetaPersonal, etiquetaImportante),
                false
        ));

        listaNotas.add(new Note(
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(etiquetaTrabajo),
                true
        ));

        listaNotas.add(new Note(
                "Reunión con cliente",
                "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                Arrays.asList(etiquetaTrabajo, etiquetaUrgente),
                false
        ));


        listaNotas.add(new Note(
                "Lista de compras",
                "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                Arrays.asList(etiquetaPersonal),
                true
        ));


        listaNotas.add(new Note(
                "Idea para proyecto",
                "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                Arrays.asList(etiquetaIdeas, etiquetaImportante),
                true
        ));

        listaNotas.add(new Note(
                "Cita médica",
                "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                Arrays.asList(etiquetaPersonal, etiquetaImportante),
                false
        ));

        listaNotas.add(new Note(
                "Plan de marketing",
                "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                Arrays.asList(etiquetaTrabajo),
                true
        ));
    }

    public List<Note> getListaNotas() {
        return listaNotas;
    }

    public void setListaNotas(List<Note> listaNotas) {
        this.listaNotas = listaNotas;
    }
}
