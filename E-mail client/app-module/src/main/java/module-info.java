module com.example.appmodule {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    requires static lombok;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.web;
    requires spring.context;
    requires spring.core;
    requires java.desktop; // Додано для ParameterizedTypeReference

    opens com.example.appmodule to javafx.fxml;
    exports com.example.appmodule;

    exports com.example.appmodule.controller;
    opens com.example.appmodule.controller to javafx.fxml;

    exports com.example.appmodule.controller.user;
    opens com.example.appmodule.controller.user to javafx.fxml;

    exports com.example.appmodule.controller.email_account;
    opens com.example.appmodule.controller.email_account to javafx.fxml;

    exports com.example.appmodule.controller.email_message;
    opens com.example.appmodule.controller.email_message to javafx.fxml;

    exports com.example.appmodule.dto.user;
    exports com.example.appmodule.service;
    exports com.example.appmodule.config;
    exports com.example.appmodule.dto;
    exports com.example.appmodule.dto.email_message;
    exports com.example.appmodule.dto.email_account;
}