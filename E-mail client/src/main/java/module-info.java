module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires static lombok;
    requires java.sql;
    requires  jakarta.mail;
    requires org.slf4j;

    opens org.example to javafx.fxml;
    exports org.example;
}