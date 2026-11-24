/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.service;

/**
 *
 * @author Ivan Elric
 */

import com.mycompany.asecnadocflow.model.Product;
import com.mycompany.asecnadocflow.util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvoiceParserService {

    private static final Pattern PATTERN_NUM_FACTURE =
            Pattern.compile("FACTURE\\s*N[^\\s:]*\\s*[:\\-]?\\s*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_DATE_BORDEREAU =
            Pattern.compile("Date\\s+bordereau\\s*:?\\s*(\\d{2}[/\\-]\\d{2}[/\\-]\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ORIGINE =
            Pattern.compile("Origine\\s*:?\\s*([A-ZÀ-Ÿ0-9\\s\\-]+?)(?:\\r?\\n|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_FOURNISSEUR =
            Pattern.compile("Fournisseur\\s*:?\\s*(.+?)(?:\\r?\\n|$)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_ZONE_TABLEAU =
            Pattern.compile("Prix\\s*total\\s*(.*?)Total\\s+arr", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PATTERN_FUSION_LIGNES =
            Pattern.compile("\\r?\\n+\\s*(?=(?:ul|[0-9+lI|]{1,4})\\s+[0-9O.,]+\\s*Ar)");
    private static final Pattern PATTERN_LIGNE_ARTICLE =
            Pattern.compile("^\\s*(.+?)\\s+((?:ul|[0-9+lI|]){1,4})\\s+([0-9O.,]+)\\s*Ar\\s+(.*)$",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public List<Product> analyserFacture(String texteBrut) {
        List<Product> articles = new ArrayList<>();
        if (texteBrut == null || texteBrut.trim().isEmpty()) {
            return articles;
        }

        String numFacture = extraireChamp(PATTERN_NUM_FACTURE, texteBrut, "n° facture non detecté");
        String dateStr = extraireChamp(PATTERN_DATE_BORDEREAU, texteBrut, "");
        LocalDate dateReception = DateUtil.parseFlexible(dateStr);
        String origine = extraireChamp(PATTERN_ORIGINE, texteBrut, "origine non trouvée");
        String fournisseur = extraireChamp(PATTERN_FOURNISSEUR, texteBrut, "ASECNA / DGRP");

        Matcher matcherZone = PATTERN_ZONE_TABLEAU.matcher(texteBrut);
        if (matcherZone.find()) {
            String brutTableau = matcherZone.group(1).trim();
            String tableauNormalise = PATTERN_FUSION_LIGNES.matcher(brutTableau).replaceAll(" ");
            Matcher matcherArticle = PATTERN_LIGNE_ARTICLE.matcher(tableauNormalise);

            while (matcherArticle.find()) {
                String designation = matcherArticle.group(1)
                        .replaceAll("[|\\[\\]_]", "")
                        .trim()
                        .replaceAll("\\s+", " ");

                int quantite = parserQuantite(matcherArticle.group(2).trim());
                double prixUnitaire = convertirMontant(matcherArticle.group(3));

                if (!designation.isEmpty()) {
                    articles.add(new Product(
                            0, numFacture, designation, quantite, prixUnitaire, fournisseur, origine, dateReception
                    ));
                }
            }
        }

        if (articles.isEmpty()) {
            articles.add(new Product(0, numFacture, "Article non détecté", 1, 0.0, fournisseur, origine, dateReception));
        }

        return articles;
    }

    private int parserQuantite(String qteBrute) {
        if ("ul".equalsIgnoreCase(qteBrute) || "+".equals(qteBrute) || "l".equals(qteBrute)
                || "I".equals(qteBrute) || "|".equals(qteBrute)) {
            return 1;
        }
        String chiffresQte = qteBrute.replaceAll("[lI|]", "1").replaceAll("[^0-9]", "");
        return chiffresQte.isEmpty() ? 1 : Integer.parseInt(chiffresQte);
    }

    private String extraireChamp(Pattern pattern, String texte, String defaut) {
        Matcher matcher = pattern.matcher(texte);
        return matcher.find() ? matcher.group(1).trim() : defaut;
    }

    private double convertirMontant(String valeur) {
        try {
            String propre = valeur
                    .replace("O", "0")
                    .replace("o", "0")
                    .replaceAll("[^0-9,.]", "")
                    .replace(",", ".");
            return propre.isEmpty() ? 0.0 : Double.parseDouble(propre);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
