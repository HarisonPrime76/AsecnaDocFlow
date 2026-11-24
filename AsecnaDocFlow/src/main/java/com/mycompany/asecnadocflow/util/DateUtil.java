/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.util;

/**
 *
 * @author Ivan Elric
 */

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    private static final DateTimeFormatter FORMAT_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtil() {}

    public static LocalDate parseFlexible(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now();
        }
        String cleanDate = dateStr.trim().replace("-", "/");
        try {
            return LocalDate.parse(cleanDate, FORMAT_FR);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateStr.trim(), FORMAT_ISO);
            } catch (DateTimeParseException e2) {
                return LocalDate.now();
            }
        }
    }

    public static String formatFr(LocalDate date) {
        return date != null ? date.format(FORMAT_FR) : "";
    }
}
