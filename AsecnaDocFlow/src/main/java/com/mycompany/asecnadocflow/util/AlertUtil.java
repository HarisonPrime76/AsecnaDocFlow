/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.util;

/**
 *
 * @author Ivan Elric
 */

import javafx.scene.control.Alert;

public class AlertUtil {

    public static void showInfo(String titre, String message) {
        show(Alert.AlertType.INFORMATION, titre, message);
    }

    public static void showError(String titre, String message) {
        show(Alert.AlertType.ERROR, titre, message);
    }

    private static void show(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
