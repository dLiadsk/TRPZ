package com.example.appmodule.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class HelloController {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    @FXML
    protected void toLoginUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/user/login-user.fxml"));
            AnchorPane root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void toCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/user/create-user.fxml"));
            AnchorPane root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
