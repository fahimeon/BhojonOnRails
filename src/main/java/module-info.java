module com.example.bhojhon {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires tess4j;
    requires java.desktop;
    requires java.net.http;
    requires com.google.gson;
    requires org.apache.pdfbox;
    requires jakarta.mail;
    requires org.eclipse.angus.mail;

    opens com.example.bhojhon to javafx.fxml;
    opens com.example.bhojhon.controller to javafx.fxml, javafx.base;
    opens com.example.bhojhon.model to javafx.base;

    exports com.example.bhojhon;
}