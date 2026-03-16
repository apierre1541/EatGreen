package com.example.eatgreen;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String role;

    // Le Constructeur : c'est ce qui permet de créer un utilisateur
    public User(int id, String nom, String prenom, String email, String role, String telephone, String mdp) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.telephone = telephone;
    }

    // Les Getters : pour que l'Adapter puisse lire les infos et les afficher
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone(){return telephone; }
    public String getRole() { return role; }
}