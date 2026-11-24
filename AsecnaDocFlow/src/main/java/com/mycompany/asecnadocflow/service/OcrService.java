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

public class OcrService {

    private static OcrService instance;
    private final Tesseract tesseract;

    private OcrService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("fra");
        tesseract.setPageSegMode(3);
    }

    public static synchronized OcrService getInstance() {
        if (instance == null) {
            instance = new OcrService();
        }
        return instance;
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
                BufferedImage imagePage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                texteComplet.append(tesseract.doOCR(imagePage)).append("\n");
            }
        }
        return texteComplet.toString();
    }
}
