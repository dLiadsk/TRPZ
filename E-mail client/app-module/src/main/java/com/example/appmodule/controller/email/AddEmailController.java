package com.example.appmodule.controller.email;

import com.example.appmodule.config.AuthenticatedUser;
import com.example.appmodule.controller.MainController;
import com.example.appmodule.service.EmailAccountService;
import com.example.appmodule.service.EmailService;
import com.example.appmodule.dto.ServerConnectionDto;
import com.example.appmodule.dto.user.UserDto;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AddEmailController {

    private final EmailAccountService emailAccountService;
    private UserDto user;
    private final AuthenticatedUser authenticatedUser;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public AddEmailController() {
        this.authenticatedUser = AuthenticatedUser.getInstance();
        this.emailAccountService = new EmailAccountService();
        this.user = authenticatedUser.getUser();
    }
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField incomingProtocolField;
    @FXML
    private TextField incomingPortField;
    @FXML
    private TextField outgoingProtocolField;
    @FXML
    private TextField outgoingPortField;
    @FXML
    private CheckBox autoconfigCheckbox;
    @FXML
    private Label errorMessage;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private VBox incomingServerBox;

    @FXML
    private VBox outgoingServerBox;


    public AddEmailController(EmailAccountService emailAccountService, EmailService emailService, AuthenticatedUser authenticatedUser) {
        this.emailAccountService = emailAccountService;
        this.authenticatedUser = authenticatedUser;
    }


    @FXML
    private void initialize() {

        toggleServerFields(autoconfigCheckbox.isSelected());

        autoconfigCheckbox.setOnAction(event -> toggleServerFields(autoconfigCheckbox.isSelected()));
        errorMessage.setVisible(false);
    }

    private void toggleServerFields(boolean isAutoconfig) {
        boolean manualConfig = !isAutoconfig;
        incomingServerBox.setVisible(manualConfig);
        outgoingServerBox.setVisible(manualConfig);
        incomingServerBox.setManaged(manualConfig);
        outgoingServerBox.setManaged(manualConfig);
    }

    @FXML
    private void handleSave() {
        String email = emailField.getText();
        String password = passwordField.getText();
        boolean autoconfig = autoconfigCheckbox.isSelected();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and Password fields must be filled.");
            return;
        }

        ServerConnectionDto incomingConnection = null;
        ServerConnectionDto outgoingConnection = null;

        if (!autoconfig) {
            try {
                int incomingPort = Integer.parseInt(incomingPortField.getText());
                int outgoingPort = Integer.parseInt(outgoingPortField.getText());

                incomingConnection = new ServerConnectionDto(
                        null,
                        incomingPort,
                        incomingProtocolField.getText()
                );

                outgoingConnection = new ServerConnectionDto(
                        null,
                        outgoingPort,
                        outgoingProtocolField.getText()
                );

                if (incomingConnection.getProtocol().isEmpty() ||
                        outgoingConnection.getProtocol().isEmpty()) {
                    showError("Protocol fields must not be empty in manual configuration.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Port values must be numeric.");
                return;
            }
        }

        try {
            UserDto userDto = emailAccountService.addEmailAccount(email, password, autoconfig, incomingConnection, outgoingConnection, user);
            authenticatedUser.setUser(userDto);
            if (mainController != null) {
                mainController.handleRefreshEmails();

                mainController.refreshEmailAccounts();
            }
            closeWindow();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            showError("Failed to add email account. Please check your input and try again.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
}
