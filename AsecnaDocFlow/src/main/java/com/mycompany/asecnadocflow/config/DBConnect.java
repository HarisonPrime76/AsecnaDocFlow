/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.asecnadocflow.config; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author Ivan Elric
 */


public class DBConnect {
     // Paramètres de connexion PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/AsecnaDB";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
