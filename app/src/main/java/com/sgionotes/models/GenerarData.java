package com.sgionotes.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerarData {

    private List<Note> listaNotas;
    private List<Tag> listaEtiquetas;

    public GenerarData(int n) {
        if (n == 1){
            listaNotas = new ArrayList<>();

            Tag tagTrabajo = new Tag("Trabajo");
            Tag tagPersonal = new Tag("Personal");
            Tag tagImportante = new Tag("Importante");
            Tag tagIdeas = new Tag("Ideas");
            Tag tagUrgente = new Tag("Urgente");

            listaNotas.add(new Note(
                    "Reunión con cliente",
                    "No olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.No olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\nNo olvidar preparar la presentación y enviarla por correo antes del viernes.\n",
                    Arrays.asList(tagTrabajo, tagUrgente, tagIdeas),
                    true
            ));


            listaNotas.add(new Note(
                    "Lista de compras",
                    "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                    Arrays.asList(tagPersonal),
                    true
            ));


            listaNotas.add(new Note(
                    "Idea para proyecto",
                    "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                    Arrays.asList(tagIdeas, tagImportante),
                    true
            ));

            listaNotas.add(new Note(
                    "Cita médica",
                    "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                    Arrays.asList(tagPersonal, tagImportante),
                    false
            ));

            listaNotas.add(new Note(
                    "Plan de marketing",
                    "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                    Arrays.asList(tagTrabajo),
                    true
            ));

            listaNotas.add(new Note(
                    "Reunión con cliente",
                    "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                    Arrays.asList(tagTrabajo, tagUrgente),
                    false
            ));


            listaNotas.add(new Note(
                    "Lista de compras",
                    "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                    Arrays.asList(tagPersonal),
                    true
            ));


            listaNotas.add(new Note(
                    "Idea para proyecto",
                    "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                    Arrays.asList(tagIdeas, tagImportante),
                    true
            ));

            listaNotas.add(new Note(
                    "Cita médica",
                    "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                    Arrays.asList(tagPersonal, tagImportante),
                    false
            ));

            listaNotas.add(new Note(
                    "Plan de marketing",
                    "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                    Arrays.asList(tagTrabajo),
                    true
            ));

            listaNotas.add(new Note(
                    "Reunión con cliente",
                    "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                    Arrays.asList(tagTrabajo, tagUrgente),
                    false
            ));


            listaNotas.add(new Note(
                    "Lista de compras",
                    "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                    Arrays.asList(tagPersonal),
                    true
            ));


            listaNotas.add(new Note(
                    "Idea para proyecto",
                    "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                    Arrays.asList(tagIdeas, tagImportante),
                    true
            ));

            listaNotas.add(new Note(
                    "Cita médica",
                    "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                    Arrays.asList(tagPersonal, tagImportante),
                    false
            ));

            listaNotas.add(new Note(
                    "Plan de marketing",
                    "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                    Arrays.asList(tagTrabajo),
                    true
            ));

            listaNotas.add(new Note(
                    "Reunión con cliente",
                    "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                    Arrays.asList(tagTrabajo, tagUrgente),
                    false
            ));


            listaNotas.add(new Note(
                    "Lista de compras",
                    "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                    Arrays.asList(tagPersonal),
                    true
            ));


            listaNotas.add(new Note(
                    "Idea para proyecto",
                    "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                    Arrays.asList(tagIdeas, tagImportante),
                    true
            ));

            listaNotas.add(new Note(
                    "Cita médica",
                    "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                    Arrays.asList(tagPersonal, tagImportante),
                    false
            ));

            listaNotas.add(new Note(
                    "Plan de marketing",
                    "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                    Arrays.asList(tagTrabajo),
                    true
            ));

            listaNotas.add(new Note(
                    "Reunión con cliente",
                    "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                    Arrays.asList(tagTrabajo, tagUrgente),
                    false
            ));


            listaNotas.add(new Note(
                    "Lista de compras",
                    "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                    Arrays.asList(tagPersonal),
                    true
            ));


            listaNotas.add(new Note(
                    "Idea para proyecto",
                    "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                    Arrays.asList(tagIdeas, tagImportante),
                    true
            ));

            listaNotas.add(new Note(
                    "Cita médica",
                    "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                    Arrays.asList(tagPersonal, tagImportante),
                    false
            ));

            listaNotas.add(new Note(
                    "Plan de marketing",
                    "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                    Arrays.asList(tagTrabajo),
                    true
            ));

            listaNotas.add(new Note(
                    "Reunión con cliente",
                    "No olvidar preparar la presentación y enviarla por correo antes del viernes.",
                    Arrays.asList(tagTrabajo, tagUrgente),
                    false
            ));


            listaNotas.add(new Note(
                    "Lista de compras",
                    "Leche, pan, huevos, arroz, pollo, verduras y frutas.",
                    Arrays.asList(tagPersonal),
                    true
            ));


            listaNotas.add(new Note(
                    "Idea para proyecto",
                    "Crear una aplicación móvil para organizar tareas mediante inteligencia artificial.",
                    Arrays.asList(tagIdeas, tagImportante),
                    true
            ));

            listaNotas.add(new Note(
                    "Cita médica",
                    "Cita con el doctor Ramírez el lunes 12 a las 9:00 a.m.",
                    Arrays.asList(tagPersonal, tagImportante),
                    false
            ));

            listaNotas.add(new Note(
                    "Plan de marketing",
                    "Definir objetivos trimestrales, estudiar la competencia y proponer campañas.",
                    Arrays.asList(tagTrabajo),
                    true
            ));
        }
        else if (n == 2) {
            listaEtiquetas = new ArrayList<>();
            listaEtiquetas.add(new Tag("Notas personales"));
            listaEtiquetas.add(new Tag("Varios"));
            listaEtiquetas.add(new Tag("Credenciales"));
            listaEtiquetas.add(new Tag("Trabajo"));
            listaEtiquetas.add(new Tag("Clase"));
            listaEtiquetas.add(new Tag("Pendientes"));
        }
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
}
