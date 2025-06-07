module org.example.pp_lab6 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires javafx.swing;
    requires java.desktop;

    opens org.example.pp_lab6 to javafx.fxml;
    exports org.example.pp_lab6;
}