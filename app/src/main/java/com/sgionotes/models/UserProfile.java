package com.sgionotes.models;

public class UserProfile {
    private String nombres;
    private String apellidos;
    private String email;
    private int profileIcon; // Resource ID del icono seleccionado

    public UserProfile() {
        // Constructor vacío para Firebase
    }

    public UserProfile(String nombres, String apellidos, String email, int profileIcon) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.profileIcon = profileIcon;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getProfileIcon() {
        return profileIcon;
    }

    public void setProfileIcon(int profileIcon) {
        this.profileIcon = profileIcon;
    }

    public String getFullName() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }
}
