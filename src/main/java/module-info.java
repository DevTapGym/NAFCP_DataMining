module com.example.source_code {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.source_code.Model to javafx.base, javafx.fxml;

    opens com.example.source_code to javafx.fxml;
    exports com.example.source_code;
    exports com.example.source_code.Controller;
    opens com.example.source_code.Controller to javafx.fxml;
}