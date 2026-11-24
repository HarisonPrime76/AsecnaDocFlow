/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.service;

/**
 *
 * @author Ivan Elric
 */

import com.mycompany.asecnadocflow.dao.ProductDAO;
import com.mycompany.asecnadocflow.model.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public boolean enregistrerOuModifier(Product product) {
        if (!estValide(product)) {
            return false;
        }
        return product.getId() <= 0 ? productDAO.insert(product) : productDAO.update(product);
    }

    public int enregistrerLot(List<Product> products) {
        if (products == null || products.isEmpty()) return 0;

        List<Product> valides = products.stream()
                .filter(p -> !"Article non détecté".equals(p.getDesignation()))
                .filter(this::estValide)
                .collect(Collectors.toList());

        return productDAO.insertBatch(valides);
    }

    public boolean supprimer(int id) {
        return id > 0 && productDAO.delete(id);
    }

    public List<Product> listerPage(int page, int lignesParPage) {
        return productDAO.findAllPaginated(page, lignesParPage);
    }

    public int compterTotal() {
        return productDAO.countAll();
    }

    private boolean estValide(Product p) {
        return p != null
                && p.getQuantite() > 0
                && p.getPrixUnitaire() >= 0
                && p.getDesignation() != null && !p.getDesignation().trim().isEmpty()
                && p.getNumFacture() != null && !p.getNumFacture().trim().isEmpty();
    }
}
