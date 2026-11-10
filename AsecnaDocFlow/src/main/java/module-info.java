module com.mycompany.asecnadocflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.mycompany.asecnadocflow.controller to javafx.fxml;
    exports com.mycompany.asecnadocflow;
    requires org.apache.pdfbox;
}
