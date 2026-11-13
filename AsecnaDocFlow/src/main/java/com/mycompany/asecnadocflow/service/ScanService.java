/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.service;

/**
 *
 * @author Ivan Elric
 */
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScanService {

    private final Tesseract tesseract;

    public ScanService() {
        tesseract = new Tesseract();
        // Dossier contenant le fichier fra.traineddata
        tesseract.setDatapath("src/main/resources/tessdata");
        // Langue française pour les factures
        tesseract.setLanguage("fra");
        // Améliore la détection des blocs de texte structurés (factures)
        tesseract.setPageSegMode(3);
    }

    public String extraireTexte(File fichier) {
        if (fichier == null || !fichier.exists()) {
            return null;
        }

        String nomFichier = fichier.getName().toLowerCase();

        try {
            if (nomFichier.endsWith(".pdf")) {
                return extraireDepuisPdf(fichier);
            } else {
                // Traitement direct pour PNG, JPG, JPEG
                return tesseract.doOCR(fichier);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extraireDepuisPdf(File fichierPdf) throws IOException, TesseractException {
        StringBuilder texteComplet = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(fichierPdf)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int nombrePages = document.getNumberOfPages();

            for (int page = 0; page < nombrePages; page++) {
                // Rendu en 300 DPI pour une lecture précise des petits caractères
                BufferedImage imagePage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String textePage = tesseract.doOCR(imagePage);
                texteComplet.append(textePage).append("\n");
            }
        }

        return texteComplet.toString();
    }
}
