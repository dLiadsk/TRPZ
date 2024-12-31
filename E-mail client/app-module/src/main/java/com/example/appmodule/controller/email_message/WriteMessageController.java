package com.example.appmodule.controller.email_message;

import com.example.appmodule.config.AuthenticatedUser;
import com.example.appmodule.controller.MainController;
import com.example.appmodule.service.EmailService;
import com.example.appmodule.dto.email.EmailAccountDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
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

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Ініціалізація списку відправників
        ObservableList<EmailAccountDto> senderOptions = FXCollections.observableArrayList(authenticatedUser.getUser().getEmailAccounts());
        senderComboBox.setItems(senderOptions);

        attachmentList.setItems(attachments);

        addAttachmentButton.setOnAction(event -> addAttachment());
        sendButton.setOnAction(event -> sendEmail());
        cancelButton.setOnAction(event -> confirmSaveDraft());
        saveDraftButton.setOnAction(event -> saveDraft(senderComboBox.getValue()));
    }

    private void addAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Оберіть файл для прикріплення");
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
            showAlert("Помилка", "Оберіть адресу відправника!", Alert.AlertType.ERROR);
            return;
        }
        if (subject.isEmpty() || recipients.isEmpty() || body.isEmpty()) {
            showAlert("Помилка", "Заповніть усі обов'язкові поля!", Alert.AlertType.ERROR);
            return;
        }
        String result = emailService.sendMessage(subject, sender, recipients, body, attachments);
        mainController.handleRefreshEmails();
        mainController.showError(result);
        navigateToMainPage();
    }

    private void confirmSaveDraft() {
        EmailAccountDto sender = senderComboBox.getValue();
        if (sender != null && !sender.getEmailAddress().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Підтвердження");
            alert.setHeaderText("Скасувати створення листа?");
            alert.setContentText("Чи бажаєте зберегти повідомлення як чернетку?");

            ButtonType saveButton = new ButtonType("Зберегти");
            ButtonType discardButton = new ButtonType("Не зберігати", ButtonBar.ButtonData.CANCEL_CLOSE);

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

    private void saveDraft( EmailAccountDto sender) {

        String subject = subjectField.getText();
        List<String> recipients = Arrays.stream(recipientField.getText().split(","))
                .map(String::trim)
                .filter(address -> !address.isEmpty())
                .toList();
        String body = bodyField.getText();

        String result = emailService.saveDraft(subject, sender, recipients, body, attachments);
        mainController.handleRefreshEmails();
        mainController.showError(result);

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
