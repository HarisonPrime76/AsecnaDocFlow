/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.model;
/**
 *
 * @author Ivan Elric
 */
public class ProductModel {
    private final int id;
    private final String numFacture;
    private final String designation;
    private final int quantite;
    private final double prixUnitaire;
    private final double prixTotal;
    private final String fournisseur;
    private final String lieuReception;
    private final String dateReception;
    
    public ProductModel(int id, String numFacture, String designation, int quantite, double prixUnitaire, double prixTotal, String fournisseur, String lieuReception, String dateReception) {
        this.id = id;
        this.numFacture = numFacture;
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.prixTotal = prixTotal;
        this.fournisseur = fournisseur;
        this.lieuReception = lieuReception;
        this.dateReception = dateReception;
    }
    
    public int getId() { return id; }
    public String getNumFacture() { return numFacture; }
    public String getDesignation() { return designation; }
    public int getQuantite() { return quantite; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public double getPrixTotal() { return prixTotal; }
    public String getFournisseur() { return fournisseur; }
    public String getLieuReception() { return lieuReception; }
    public String getDateReception() { return dateReception; }
}