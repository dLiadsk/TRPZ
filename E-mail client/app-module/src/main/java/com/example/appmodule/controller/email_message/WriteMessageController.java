package com.example.appmodule.controller.email_message;

import com.example.appmodule.config.AuthenticatedUser;
import com.example.appmodule.controller.MainController;
import com.example.appmodule.dto.email_message.EmailMessageDto;
import com.example.appmodule.service.EmailService;
import com.example.appmodule.dto.email_account.EmailAccountDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class WriteMessageController {
    private final AuthenticatedUser authenticatedUser;
    private final EmailService emailService;
    private final ObservableList<String> attachments = FXCollections.observableArrayList();
    private MainController mainController;
    @FXML
    private ComboBox<EmailAccountDto> senderComboBox;
    @FXML
    private TextField subjectField;
    @FXML
    private TextField recipientField;
    @FXML
    private TextArea bodyField;
    @FXML
    private ListView<String> attachmentList;
    @FXML
    private Button sendButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button addAttachmentButton;
    @FXML
    private Button saveDraftButton;

    public WriteMessageController() {
        this.emailService = new EmailService();
        this.authenticatedUser = AuthenticatedUser.getInstance();
    }

    public void setEmailMessageContext(EmailMessageDto emailMessageContext) {
        EmailAccountDto emailAccountDto = authenticatedUser.getUser().getEmailAccounts().stream()
                .filter(emailAccountDto1 -> emailAccountDto1.getEmailAddress().equals(emailMessageContext.getFrom()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Email address not found"));
        senderComboBox.setValue(emailAccountDto);
        subjectField.setText(emailMessageContext.getSubject());
        recipientField.setText(String.join(", ", emailMessageContext.getTo().toString())
                .replace("[", "")
                .replace("]", ""));
        bodyField.setText(emailMessageContext.getBody());
        attachmentList.setItems(FXCollections.observableArrayList(emailMessageContext.getAttachmentPaths()));

    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Ініціалізація списку відправників
        ObservableList<EmailAccountDto> senderOptions = FXCollections.observableArrayList(authenticatedUser.getUser().getEmailAccounts());
        senderComboBox.setItems(senderOptions);


        addAttachmentButton.setOnAction(event -> addAttachment());
        sendButton.setOnAction(event -> sendEmail());
        cancelButton.setOnAction(event -> confirmSaveDraft());
        saveDraftButton.setOnAction(event -> saveDraft(senderComboBox.getValue()));
    }

    private void addAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to attach.");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(addAttachmentButton.getScene().getWindow());

        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                attachments.add(file.getAbsolutePath());
            }
        }
    }

    private void sendEmail() {
        EmailAccountDto sender = senderComboBox.getValue();
        String subject = subjectField.getText();
        List<String> recipients = Arrays.stream(recipientField.getText().split(","))
                .map(String::trim)
                .filter(address -> !address.isEmpty())
                .toList();
        String body = bodyField.getText();

        if (sender == null || sender.getEmailAddress().isEmpty()) {
            showAlert("Error", "Choose a sender address!", Alert.AlertType.ERROR);
            return;
        }
        if (subject.isEmpty() || recipients.isEmpty() || body.isEmpty()) {
            showAlert("Error", "Fill in all required fields!", Alert.AlertType.ERROR);
            return;
        }
        String result = emailService.sendMessage(subject, sender, recipients, body, attachments);
        if (mainController != null) {
            mainController.handleRefreshEmails();
            mainController.showError(result);
        }

        navigateToMainPage();
    }

    private void confirmSaveDraft() {
        EmailAccountDto sender = senderComboBox.getValue();
        if (sender != null && !sender.getEmailAddress().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Cancel creating email?");
            alert.setContentText("Do you want to save the message as a draft?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Do not save", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == saveButton) {
                    saveDraft(sender);

                }
                cancel();
            });
        }
        navigateToMainPage();
    }

    private void saveDraft(EmailAccountDto sender) {

        String subject = subjectField.getText();
        List<String> recipients = Arrays.stream(recipientField.getText().split(","))
                .map(String::trim)
                .filter(address -> !address.isEmpty())
                .toList();
        String body = bodyField.getText();

        String result = emailService.saveDraft(subject, sender, recipients, body, attachments);
        if (mainController != null) {
            mainController.handleRefreshEmails();
            mainController.showError(result);
        }

    }

    private void cancel() {
        // Закрити вікно створення листа
        addAttachmentButton.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToMainPage() {
        try {
            // Завантажуємо FXML файл для головної сторінки
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/main.fxml"));
            BorderPane root = loader.load();

            // Створюємо нову сцену для головної сторінки
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) bodyField.getScene().getWindow(); // отримуємо поточне вікно
            stage.setScene(scene); // змінюємо сцену на головну сторінку

            // Покажемо нову сцену
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
