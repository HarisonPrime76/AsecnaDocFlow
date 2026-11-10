/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.controller;

import com.mycompany.asecnadocflow.request.Request;
import com.mycompany.asecnadocflow.model.ProductModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Ivan Elric
 */

public class HomeController {

    @FXML private TableView<ProductModel> factureTable;
    @FXML private TableColumn<ProductModel, String> colNumFacture;
    @FXML private TableColumn<ProductModel, String> colDesignation;
    @FXML private TableColumn<ProductModel, Integer> colQuantite;
    @FXML private TableColumn<ProductModel, Double> colPrixUnitaire;
    @FXML private TableColumn<ProductModel, Double> colPrixTotal;
    @FXML private TableColumn<ProductModel, String> colFournisseur;
    @FXML private TableColumn<ProductModel, String> colDateReception;
    @FXML private TableColumn<ProductModel, String> colLieuReception;
    @FXML private TableColumn<ProductModel, Void> colAction;
    @FXML private Pagination pagination;

    @FXML private TextField numFactureField;
    @FXML private TextField designationField;
    @FXML private TextField fournisseurField;
    @FXML private TextField quantiteField;
    @FXML private TextField puField;
    @FXML private DatePicker dateReceptionPicker;
    @FXML private TextField lieuReceptionField;

    private final Request request = new Request();
    private final int LIGNES_PAR_PAGE = 15;
    private int produitIdEnCoursModif = -1;

    @FXML
    public void initialize() {
        initialiserColonnesTableau();
        configurerPagination();
    }

    public void initialiserColonnesTableau() {
        colNumFacture.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNumFacture()));
        colDesignation.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDesignation()));
        colQuantite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantite()));
        colPrixUnitaire.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrixUnitaire()));
        colPrixTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrixTotal()));
        colFournisseur.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFournisseur()));
        colLieuReception.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLieuReception()));
        colDateReception.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDateReception()));
    
        if (colAction != null) {
            configurerColonneActions(colAction);
        }
    }
    
    

    private void configurerPagination() {
        int totalProduits = this.request.compterTotalProduits();
        int nombreTotalPages = (int) Math.ceil((double) totalProduits / LIGNES_PAR_PAGE);
        pagination.setPageCount(nombreTotalPages > 0 ? nombreTotalPages : 1);

        pagination.setPageFactory(pageIndex -> {
            chargerPageProduits(pageIndex + 1);
        return new VBox();
        });
    }

    private void chargerPageProduits(int numeroPage) {
        List<ProductModel> produitsPage = request.getAllDocument(numeroPage, LIGNES_PAR_PAGE);
        factureTable.getItems().setAll(produitsPage);
    }

    private void rafraichirDonnees() {
        configurerPagination();
        chargerPageProduits(pagination.getCurrentPageIndex() + 1);
    }

    @FXML
    public void handleEnregistrerProduit() {
        String numFacture = numFactureField.getText();
        String designation = designationField.getText();
        String fournisseur = fournisseurField.getText();
        String qteStr = quantiteField.getText();
        String puStr = puField.getText();
        LocalDate dateRec = dateReceptionPicker.getValue();
        String lieu = lieuReceptionField.getText();

        if (!validerChampsTexte(numFacture, designation, fournisseur, qteStr, puStr)) {
            return;
        }

        try {
            int quantite = Integer.parseInt(qteStr);
            double prixUnitaire = Double.parseDouble(puStr);
            double prixTotal = quantite * prixUnitaire;
            String dateReception = dateRec != null ? dateRec.toString() : "";

            boolean resultat;
            String messageSucces;

            if (produitIdEnCoursModif == -1) {
                resultat = this.request.AddDocument(numFacture, designation, quantite, prixUnitaire, prixTotal, fournisseur, lieu, dateReception);
                messageSucces = "Facture enregistrée avec succès !";
            } else {
                resultat = this.request.EditDocument(produitIdEnCoursModif, numFacture, designation, quantite, prixUnitaire, prixTotal, fournisseur, lieu, dateReception);
                messageSucces = "Facture modifiée avec succès !";
                produitIdEnCoursModif = -1;
            }

            gererReponseAction(resultat, messageSucces);
            if (resultat) {
                viderChampsFormulaire();
            }

        } catch (NumberFormatException e) {
            afficherMessageModulable("Erreur de format", "La quantité et le prix unitaire doivent être des nombres valides.", Alert.AlertType.ERROR);
        }
    }

    private void configurerColonneActions(TableColumn<ProductModel, Void> colActions) {
        colActions.setCellFactory(param -> new TableCell<ProductModel, Void>() {
            private final Button btnEditer = creerBouton("Éditer", "#3498db");
            private final Button btnSupprimer = creerBouton("Supprimer", "#e74c3c");
            private final HBox conteneurBoutons = new HBox(10, btnEditer, btnSupprimer);

            {
                btnSupprimer.setOnAction(event -> {
                    ProductModel product = getTableView().getItems().get(getIndex());
                    traiterSuppression(product);
                });

                btnEditer.setOnAction(event -> {
                    ProductModel product = getTableView().getItems().get(getIndex());
                    traiterPreparationEdition(product);
                });

                conteneurBoutons.setVisible(false);
                setOnMouseEntered(e -> { if (!isEmpty()) conteneurBoutons.setVisible(true); });
                setOnMouseExited(e -> conteneurBoutons.setVisible(false));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : conteneurBoutons);
            }
        });
    }

    private void traiterSuppression(ProductModel product) {
        boolean succes = this.request.DeleteDocument(product.getId());
        if (succes) {
            afficherMessageModulable("Succès", "Facture supprimée.", Alert.AlertType.INFORMATION);
            rafraichirDonnees();
        } else {
            afficherMessageModulable("Erreur", "Suppression impossible.", Alert.AlertType.ERROR);
        }
    }

    private void traiterPreparationEdition(ProductModel product) {
        produitIdEnCoursModif = product.getId();
        numFactureField.setText(product.getNumFacture());
        designationField.setText(product.getDesignation());
        fournisseurField.setText(product.getFournisseur());
        quantiteField.setText(String.valueOf(product.getQuantite()));
        puField.setText(String.valueOf(product.getPrixUnitaire()));
        lieuReceptionField.setText(product.getLieuReception());
        if (product.getDateReception() != null && !product.getDateReception().isEmpty()) {
            dateReceptionPicker.setValue(LocalDate.parse(product.getDateReception()));
        }
    }
    @FXML
    private void viderChampsFormulaire() {
        numFactureField.clear();
        designationField.clear();
        fournisseurField.clear();
        quantiteField.clear();
        puField.clear();
        dateReceptionPicker.setValue(null);
        lieuReceptionField.clear();
        produitIdEnCoursModif = -1;
    }

    private Button creerBouton(String texte, String couleurHex) {
        Button bouton = new Button(texte);
        bouton.setStyle("-fx-background-color: " + couleurHex + "; -fx-text-fill: white; -fx-cursor: hand;");
        return bouton;
    }

    private void afficherMessageModulable(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validerChampsTexte(String... champs) {
        for (String champ : champs) {
            if (champ == null || champ.trim().isEmpty()) {
                afficherMessageModulable("Champ(s) vide(s)", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
                return false;
            }
        }
        return true;
    }

    private void gererReponseAction(boolean succes, String messageSucces) {
        if (succes) {
            afficherMessageModulable("Succès", messageSucces, Alert.AlertType.INFORMATION);
            rafraichirDonnees();
        } else {
            afficherMessageModulable("Erreur", "L'opération a échoué dans la base de données.", Alert.AlertType.ERROR);
        }
    }

}