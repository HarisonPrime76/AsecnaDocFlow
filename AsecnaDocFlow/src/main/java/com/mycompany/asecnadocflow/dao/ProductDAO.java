/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.dao;
/**
 *
 * @author Ivan Elric
 */

import com.mycompany.asecnadocflow.config.DBConnect;
import com.mycompany.asecnadocflow.model.Product;
import com.mycompany.asecnadocflow.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String SQL_INSERT =
            "INSERT INTO product (num_facture, designation, quantite, prix_unitaire, prix_total, fournisseur, lieu_reception, date_reception) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE product SET num_facture = ?, designation = ?, quantite = ?, prix_unitaire = ?, prix_total = ?, fournisseur = ?, lieu_reception = ?, date_reception = ? WHERE id = ?";
    private static final String SQL_DELETE =
            "DELETE FROM product WHERE id = ?";
    private static final String SQL_SELECT_PAGINATED =
            "SELECT * FROM product ORDER BY id DESC LIMIT ? OFFSET ?";
    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM product";
    private static final String SQL_SEARCH_PAGINATED =
        "SELECT * FROM product WHERE num_facture LIKE ? OR designation LIKE ? OR fournisseur LIKE ? OR lieu_reception LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?";
private static final String SQL_COUNT_SEARCH =
        "SELECT COUNT(*) FROM product WHERE num_facture LIKE ? OR designation LIKE ? OR fournisseur LIKE ? OR lieu_reception LIKE ?";

    public boolean insert(Product product) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(SQL_INSERT)) {
            remplirStatement(prep, product);
            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur d'insertion : " + e.getMessage());
            return false;
        }
    }

    public int insertBatch(List<Product> products) {
        if (products == null || products.isEmpty()) return 0;

        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement prep = conn.prepareStatement(SQL_INSERT)) {
                for (Product product : products) {
                    remplirStatement(prep, product);
                    prep.addBatch();
                }
                int[] resultats = prep.executeBatch();
                conn.commit();

                int totalAjouts = 0;
                for (int res : resultats) {
                    if (res >= 0 || res == PreparedStatement.SUCCESS_NO_INFO) {
                        totalAjouts++;
                    }
                }
                return totalAjouts;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Rollback lors de l'insertion par lot : " + e.getMessage());
                return 0;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion Batch : " + e.getMessage());
            return 0;
        }
    }

    public boolean update(Product product) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(SQL_UPDATE)) {
            remplirStatement(prep, product);
            prep.setInt(9, product.getId());
            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur de modification : " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(SQL_DELETE)) {
            prep.setInt(1, id);
            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur de suppression : " + e.getMessage());
            return false;
        }
    }

    public List<Product> findAllPaginated(int page, int numberPerPage) {
        List<Product> productList = new ArrayList<>();
        int offset = Math.max(0, (page - 1) * numberPerPage);

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement prep = conn.prepareStatement(SQL_SELECT_PAGINATED)) {
            prep.setInt(1, numberPerPage);
            prep.setInt(2, offset);

            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    productList.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans findAllPaginated : " + e.getMessage());
        }
        return productList;
    }

    public int countAll() {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur comptage : " + e.getMessage());
            return 0;
        }
    }

    private void remplirStatement(PreparedStatement prep, Product product) throws SQLException {
        prep.setString(1, product.getNumFacture());
        prep.setString(2, product.getDesignation());
        prep.setInt(3, product.getQuantite());
        prep.setDouble(4, product.getPrixUnitaire());
        prep.setDouble(5, product.getPrixTotal());
        prep.setString(6, product.getFournisseur());
        prep.setString(7, product.getLieuReception());
        prep.setString(8, product.getDateReception() != null ? product.getDateReception().toString() : "");
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("num_facture"),
                rs.getString("designation"),
                rs.getInt("quantite"),
                rs.getDouble("prix_unitaire"),
                rs.getString("fournisseur"),
                rs.getString("lieu_reception"),
                DateUtil.parseFlexible(rs.getString("date_reception"))
        );
    }

// Ajoutez ces deux méthodes dans ProductDAO.java
public List<Product> searchPaginated(String keyword, int page, int numberPerPage) {
    List<Product> productList = new ArrayList<>();
    int offset = Math.max(0, (page - 1) * numberPerPage);
    String pattern = "%" + keyword.trim() + "%";

    try (Connection conn = DBConnect.getConnection();
         PreparedStatement prep = conn.prepareStatement(SQL_SEARCH_PAGINATED)) {

        prep.setString(1, pattern);
        prep.setString(2, pattern);
        prep.setString(3, pattern);
        prep.setString(4, pattern);
        prep.setInt(5, numberPerPage);
        prep.setInt(6, offset);

        try (ResultSet rs = prep.executeQuery()) {
            while (rs.next()) {
                productList.add(mapResultSetToProduct(rs));
            }
        }
    } catch (SQLException e) {
        System.err.println("Erreur SQL dans searchPaginated : " + e.getMessage());
    }
    return productList;
}

public int countByKeyword(String keyword) {
    String pattern = "%" + keyword.trim() + "%";

    try (Connection conn = DBConnect.getConnection();
         PreparedStatement prep = conn.prepareStatement(SQL_COUNT_SEARCH)) {

        prep.setString(1, pattern);
        prep.setString(2, pattern);
        prep.setString(3, pattern);
        prep.setString(4, pattern);

        try (ResultSet rs = prep.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    } catch (SQLException e) {
        System.err.println("Erreur comptage recherche : " + e.getMessage());
        return 0;
    }
}

}
