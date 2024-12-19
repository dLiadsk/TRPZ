package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Label clickMeLabel;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Hello World!");
    }

    @FXML
    protected void onClickButtonClick() {
        clickMeLabel.setText("Test button");
    }
}
