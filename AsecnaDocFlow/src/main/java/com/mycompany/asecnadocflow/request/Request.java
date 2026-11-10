/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.request;
/**
 *
 * @author Ivan Elric
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.asecnadocflow.config.DBConnect;
import com.mycompany.asecnadocflow.model.ProductModel;

public class Request {
    
    public boolean AddDocument(String numFacture, String designation, int quantite, double prixUnitaire, double prixTotal, String fournisseur, String lieuReception, String dateReception) {
        String sql = "INSERT INTO product (num_facture, designation, quantite, prix_unitaire, prix_total, fournisseur, lieu_reception, date_reception) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            
            prep.setString(1, numFacture);
            prep.setString(2, designation);
            prep.setInt(3, quantite);
            prep.setDouble(4, prixUnitaire);
            prep.setDouble(5, prixTotal);
            prep.setString(6, fournisseur);
            prep.setString(7, lieuReception);
            prep.setString(8, dateReception);
            
            int data = prep.executeUpdate();
            return data > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur d'insertion : " + e.getMessage());
            return false;
        }
    }
    
    public boolean EditDocument(int id, String numFacture, String designation, int quantite, double prixUnitaire, double prixTotal, String fournisseur, String lieuReception, String dateReception) {
        String sql = "UPDATE product SET num_facture = ?, designation = ?, quantite = ?, prix_unitaire = ?, prix_total = ?, fournisseur = ?, lieu_reception = ?, date_reception = ? WHERE id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(sql)) {
              
            prep.setString(1, numFacture);
            prep.setString(2, designation);
            prep.setInt(3, quantite);
            prep.setDouble(4, prixUnitaire);
            prep.setDouble(5, prixTotal);
            prep.setString(6, fournisseur);
            prep.setString(7, lieuReception);
            prep.setString(8, dateReception);
            prep.setInt(9, id);
            
            int data = prep.executeUpdate();
            return data > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur de modification : " + e.getMessage());
            return false;
        }
    }
    
    public boolean DeleteDocument(int id) {
        String sql = "DELETE FROM product WHERE id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(sql)) {
              
            prep.setInt(1, id);
            int data = prep.executeUpdate();
            return data > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur de suppression : " + e.getMessage());
            return false;
        }
    }
    
    public List<ProductModel> getAllDocument(int page, int numberPerPage) {
        List<ProductModel> productList = new ArrayList<>();
        int offset = (page - 1) * numberPerPage;
        String sql = "SELECT * FROM product ORDER BY id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(sql)) {
              
            prep.setInt(1, numberPerPage);
            prep.setInt(2, offset);
            
            try (ResultSet result = prep.executeQuery()) {
                while (result.next()) {
                    productList.add(new ProductModel(
                        result.getInt("id"),
                        result.getString("num_facture"),
                        result.getString("designation"),
                        result.getInt("quantite"),
                        result.getDouble("prix_unitaire"),
                        result.getDouble("prix_total"),
                        result.getString("fournisseur"),
                        result.getString("lieu_reception"),
                        result.getString("date_reception")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur de recuperation : " + e.getMessage());
            System.err.println("ERREUR SQL DANS getAllDocument :");
            e.printStackTrace();
        }
        return productList;
    }
    
     public int compterTotalProduits() {
        String sql = "SELECT COUNT(*) FROM product";
        try (var conn = DBConnect.getConnection();
             var pstmt = conn.prepareStatement(sql);
             var rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("Erreur comptage : " + e.getMessage());
        }
        return 0;
    }
}