module com.mycompany.asecnadocflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires org.apache.pdfbox;

    opens com.mycompany.asecnadocflow.controller to javafx.fxml;
    exports com.mycompany.asecnadocflow;
    requires tess4j;
    
}
