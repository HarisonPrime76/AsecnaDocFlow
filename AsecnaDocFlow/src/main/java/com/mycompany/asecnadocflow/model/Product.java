/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.model;
/**
 *
 * @author Ivan Elric
 */

import java.time.LocalDate;

public class Product {
    private int id;
    private String numFacture;
    private String designation;
    private int quantite;
    private double prixUnitaire;
    private String fournisseur;
    private String lieuReception;
    private LocalDate dateReception;

    public Product() {}

    public Product(int id, String numFacture, String designation, int quantite, double prixUnitaire, String fournisseur, String lieuReception, LocalDate dateReception) {
        this.id = id;
        this.numFacture = numFacture;
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.fournisseur = fournisseur;
        this.lieuReception = lieuReception;
        this.dateReception = dateReception;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumFacture() { return numFacture; }
    public void setNumFacture(String numFacture) { this.numFacture = numFacture; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public double getPrixTotal() { return quantite * prixUnitaire; }

    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }

    public String getLieuReception() { return lieuReception; }
    public void setLieuReception(String lieuReception) { this.lieuReception = lieuReception; }

    public LocalDate getDateReception() { return dateReception; }
    public void setDateReception(LocalDate dateReception) { this.dateReception = dateReception; }
}
