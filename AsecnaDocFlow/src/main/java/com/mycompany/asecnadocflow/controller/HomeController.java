/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.controller;

import com.mycompany.asecnadocflow.model.Product;
import com.mycompany.asecnadocflow.service.InvoiceParserService;
import com.mycompany.asecnadocflow.service.OcrService;
import com.mycompany.asecnadocflow.service.ProductService;
import com.mycompany.asecnadocflow.util.AlertUtil;
import com.mycompany.asecnadocflow.util.DateUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;


/**
 *
 * @author Ivan Elric
 */

public class HomeController {

    @FXML private TableView<Product> factureTable;
    @FXML private TableColumn<Product, String> colNumFacture;
    @FXML private TableColumn<Product, String> colDesignation;
    @FXML private TableColumn<Product, Integer> colQuantite;
    @FXML private TableColumn<Product, Double> colPrixUnitaire;
    @FXML private TableColumn<Product, Double> colPrixTotal;
    @FXML private TableColumn<Product, String> colFournisseur;
    @FXML private TableColumn<Product, String> colDateReception;
    @FXML private TableColumn<Product, String> colLieuReception;
    @FXML private TableColumn<Product, Void> colAction;
    @FXML private Pagination pagination;

    @FXML private TextField numFactureField;
    @FXML private TextField designationField;
    @FXML private TextField fournisseurField;
    @FXML private TextField quantiteField;
    @FXML private TextField puField;
    @FXML private DatePicker dateReceptionPicker;
    @FXML private TextField lieuReceptionField;
    @FXML private Button btnEnregistrer;

    @FXML private Button btnScanOcr;
    @FXML private CheckBox autoSaveOcrCheckbox;
    @FXML private Label ocrProgressLabel;

    private final ProductService productService = new ProductService();
    private final InvoiceParserService parserService = new InvoiceParserService();
    private final ExecutorService ocrExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ASECNA-OCR-Worker");
        t.setDaemon(true);
        return t;
    });

    private static final int LIGNES_PAR_PAGE = 50;
    private static final DecimalFormat FORMAT_PRIX = creerFormateurPrix();

    private int produitIdEnCoursModif = -1;
    private final List<Product> fileAttenteOcr = new ArrayList<>();
    private int indexArticleActuel = 0;

    @FXML
    public void initialize() {
        configurerFiltresSaisie();
        initialiserColonnesTableau();
        configurerPagination();
        actualiserEtatBoutonEnregistrer();
    }

    private static DecimalFormat creerFormateurPrix() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.00", symbols);
    }

    private void configurerFiltresSaisie() {
        UnaryOperator<TextFormatter.Change> filtreEntier = change ->
                change.getControlNewText().matches("\\d*") ? change : null;
        quantiteField.setTextFormatter(new TextFormatter<>(filtreEntier));

        UnaryOperator<TextFormatter.Change> filtreDecimal = change ->
                change.getControlNewText().matches("\\d*([.,]\\d*)?") ? change : null;
        puField.setTextFormatter(new TextFormatter<>(filtreDecimal));
    }

    private void initialiserColonnesTableau() {
        colNumFacture.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumFacture()));
        colDesignation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignation()));
        colQuantite.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantite()));
        colPrixUnitaire.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrixUnitaire()));
        colPrixTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrixTotal()));
        colFournisseur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFournisseur()));
        colLieuReception.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLieuReception()));
        colDateReception.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatFr(c.getValue().getDateReception())));

        formaterColonneMonetaire(colPrixUnitaire);
        formaterColonneMonetaire(colPrixTotal);

        if (colAction != null) {
            configurerColonneActions(colAction);
        }
    }

    private void formaterColonneMonetaire(TableColumn<Product, Double> colonne) {
        colonne.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                } else {
                    setText(FORMAT_PRIX.format(prix) + " Ar");
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void configurerPagination() {
        int totalPages = calculerNombrePages();
        pagination.setPageCount(totalPages);
        pagination.setPageFactory(pageIndex -> {
            chargerPageProduits(pageIndex + 1);
            return new VBox();
        });
    }

    private void rafraichirDonnees() {
        int totalPages = calculerNombrePages();
        if (pagination.getPageCount() != totalPages) {
            pagination.setPageCount(totalPages);
        }
        int pageCourante = Math.min(pagination.getCurrentPageIndex() + 1, totalPages);
        chargerPageProduits(pageCourante);
    }

    private int calculerNombrePages() {
        int totalProduits = productService.compterTotal();
        int pages = (int) Math.ceil((double) totalProduits / LIGNES_PAR_PAGE);
        return Math.max(pages, 1);
    }

    private void chargerPageProduits(int numeroPage) {
        factureTable.getItems().setAll(productService.listerPage(numeroPage, LIGNES_PAR_PAGE));
    }

    @FXML
    public void handleEnregistrerProduit() {
        if (!validerFormulaire()) {
            return;
        }

        int quantite = Integer.parseInt(quantiteField.getText().trim());
        double prixUnitaire = Double.parseDouble(puField.getText().trim().replace(",", "."));
        boolean estCreation = (produitIdEnCoursModif == -1);

        Product product = new Product(
                estCreation ? 0 : produitIdEnCoursModif,
                numFactureField.getText().trim(),
                designationField.getText().trim(),
                quantite,
                prixUnitaire,
                fournisseurField.getText().trim(),
                lieuReceptionField.getText() != null ? lieuReceptionField.getText().trim() : "",
                dateReceptionPicker.getValue() != null ? dateReceptionPicker.getValue() : LocalDate.now()
        );

        if (productService.enregistrerOuModifier(product)) {
            AlertUtil.showInfo("Succès", estCreation ? "Facture enregistrée avec succès !" : "Facture modifiée avec succès !");
            produitIdEnCoursModif = -1;
            actualiserEtatBoutonEnregistrer();
            rafraichirDonnees();

            if (estCreation && !fileAttenteOcr.isEmpty()) {
                indexArticleActuel++;
                afficherArticleSuivantDansFormulaire();
            } else {
                nettoyerChampsUniquement();
            }
        } else {
            AlertUtil.showError("Erreur", "L'opération a échoué. Vérifiez que la quantité est supérieure à 0.");
        }
    }

    private boolean validerFormulaire() {
        if (estVide(numFactureField) || estVide(designationField) || estVide(fournisseurField)
                || estVide(quantiteField) || estVide(puField)) {
            AlertUtil.showError("Champ(s) vide(s)", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        try {
            int qte = Integer.parseInt(quantiteField.getText().trim());
            double pu = Double.parseDouble(puField.getText().trim().replace(",", "."));
            if (qte <= 0 || pu < 0) {
                AlertUtil.showError("Valeur invalide", "La quantité doit être supérieure à 0 et le prix positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur de format", "La quantité et le prix unitaire doivent être des nombres valides.");
            return false;
        }
        return true;
    }

    private boolean estVide(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void traiterPreparationEdition(Product product) {
        produitIdEnCoursModif = product.getId();
        remplirFormulaire(product);
        actualiserEtatBoutonEnregistrer();
    }

    private void traiterSuppression(Product product) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Supprimer l'article « " + product.getDesignation() + " » (Facture " + product.getNumFacture() + ") ?");

        Optional<ButtonType> choix = confirmation.showAndWait();
        if (choix.isPresent() && choix.get() == ButtonType.OK) {
            if (productService.supprimer(product.getId())) {
                if (produitIdEnCoursModif == product.getId()) {
                    nettoyerChampsUniquement();
                }
                AlertUtil.showInfo("Succès", "Facture supprimée.");
                rafraichirDonnees();
            } else {
                AlertUtil.showError("Erreur", "Suppression impossible.");
            }
        }
    }

    private void remplirFormulaire(Product product) {
        numFactureField.setText(product.getNumFacture());
        designationField.setText(product.getDesignation());
        fournisseurField.setText(product.getFournisseur());
        quantiteField.setText(String.valueOf(product.getQuantite()));
        puField.setText(String.valueOf(product.getPrixUnitaire()));
        lieuReceptionField.setText(product.getLieuReception());
        dateReceptionPicker.setValue(product.getDateReception());
    }

    @FXML
    private void viderChampsFormulaire() {
        nettoyerChampsUniquement();
        fileAttenteOcr.clear();
        indexArticleActuel = 0;
        majLabelProgression("", "");
    }

    private void nettoyerChampsUniquement() {
        numFactureField.clear();
        designationField.clear();
        fournisseurField.clear();
        quantiteField.clear();
        puField.clear();
        dateReceptionPicker.setValue(null);
        lieuReceptionField.clear();
        produitIdEnCoursModif = -1;
        actualiserEtatBoutonEnregistrer();
    }

    private void actualiserEtatBoutonEnregistrer() {
        if (btnEnregistrer != null) {
            btnEnregistrer.setText(produitIdEnCoursModif == -1 ? "Enregistrer" : "Mettre à jour");
        }
    }

    private void configurerColonneActions(TableColumn<Product, Void> colActions) {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditer = creerBoutonAction("Edt", "#3498db", "Modifier cette ligne");
            private final Button btnImprimer = creerBoutonAction("Prt", "#f39c12", "Imprimer le reçu");
            private final Button btnSupprimer = creerBoutonAction("Del", "#e74c3c", "Supprimer cette ligne");
            private final HBox conteneurBoutons = new HBox(8, btnEditer, btnImprimer, btnSupprimer);

            {
                conteneurBoutons.setAlignment(Pos.CENTER);
                btnSupprimer.setOnAction(e -> traiterSuppression(getTableRow().getItem()));
                btnEditer.setOnAction(e -> traiterPreparationEdition(getTableRow().getItem()));
                btnImprimer.setOnAction(e -> { /* Prêt pour l'export PDF */ });

                conteneurBoutons.setVisible(false);
                setOnMouseEntered(e -> { if (!isEmpty()) conteneurBoutons.setVisible(true); });
                setOnMouseExited(e -> conteneurBoutons.setVisible(false));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : conteneurBoutons);
            }
        });
    }

    private Button creerBoutonAction(String texte, String couleurHex, String tooltipText) {
        Button bouton = new Button(texte);
        bouton.setStyle("-fx-background-color: " + couleurHex + "; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        bouton.setTooltip(new Tooltip(tooltipText));
        return bouton;
    }

    @FXML
    private void handleOcrScan() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une facture (Image ou PDF)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents compatibles", "*.pdf", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File fichier = fileChooser.showOpenDialog(btnScanOcr.getScene().getWindow());
        if (fichier == null) return;

        btnScanOcr.setDisable(true);
        String ancienTexte = btnScanOcr.getText();
        btnScanOcr.setText("Scan en cours...");
        majLabelProgression("Analyse OCR en cours, veuillez patienter...", "#E65100");

        Task<String> ocrTask = new Task<>() {
            @Override
            protected String call() {
                return OcrService.getInstance().extraireTexte(fichier);
            }
        };

        ocrTask.setOnSucceeded(e -> {
            btnScanOcr.setDisable(false);
            btnScanOcr.setText(ancienTexte);
            traiterResultatOcr(ocrTask.getValue());
        });

        ocrTask.setOnFailed(e -> {
            btnScanOcr.setDisable(false);
            btnScanOcr.setText(ancienTexte);
            majLabelProgression("", "");
            AlertUtil.showError("Erreur OCR", "Une erreur est survenue pendant l'analyse du document.");
        });

        ocrExecutor.submit(ocrTask);
    }

    private void traiterResultatOcr(String texteBrut) {
        if (texteBrut == null || texteBrut.trim().isEmpty()) {
            majLabelProgression("", "");
            AlertUtil.showError("Erreur OCR", "Aucun texte n'a pu être extrait du document.");
            return;
        }

        List<Product> articles = parserService.analyserFacture(texteBrut);
        boolean aucunArticleValide = articles.size() == 1 && "Article non détecté".equals(articles.get(0).getDesignation());

        if (autoSaveOcrCheckbox != null && autoSaveOcrCheckbox.isSelected()) {
            int ajouts = productService.enregistrerLot(articles);
            if (ajouts > 0) {
                AlertUtil.showInfo("Succès", ajouts + " article(s) scanné(s) et enregistré(s) automatiquement.");
                rafraichirDonnees();
            } else {
                AlertUtil.showError("Erreur", "Aucune ligne valide du tableau n'a pu être enregistrée.");
            }
            majLabelProgression("", "");
        } else {
            produitIdEnCoursModif = -1;
            actualiserEtatBoutonEnregistrer();
            fileAttenteOcr.clear();
            fileAttenteOcr.addAll(articles);
            indexArticleActuel = 0;
            afficherArticleSuivantDansFormulaire();

            if (aucunArticleValide) {
                AlertUtil.showInfo("Scan partiel", "L'en-tête a été extrait, mais aucun article n'a été détecté. Saisissez la désignation manuellement.");
            } else {
                AlertUtil.showInfo("Scan OCR Réussi", articles.size() + " article(s) détecté(s). Vérifiez et enregistrez-les.");
            }
        }
    }

    private void afficherArticleSuivantDansFormulaire() {
        if (indexArticleActuel < fileAttenteOcr.size()) {
            remplirFormulaire(fileAttenteOcr.get(indexArticleActuel));
            majLabelProgression(
                    "Enregistrés : " + indexArticleActuel + " / " + fileAttenteOcr.size() +
                    "  |  Article " + (indexArticleActuel + 1) + " sur " + fileAttenteOcr.size(),
                    "#1565C0"
            );
        } else {
            majLabelProgression("Terminé : " + fileAttenteOcr.size() + " / " + fileAttenteOcr.size() + " article(s) enregistré(s) !", "#2E7D32");
            nettoyerChampsUniquement();
            fileAttenteOcr.clear();
            indexArticleActuel = 0;
        }
    }

    private void majLabelProgression(String texte, String couleurHex) {
        if (ocrProgressLabel != null) {
            ocrProgressLabel.setText(texte);
            ocrProgressLabel.setStyle(couleurHex.isEmpty() ? "" : "-fx-text-fill: " + couleurHex + "; -fx-font-weight: bold;");
        }
    }
}
