package com.example.appmodule.controller.email_message;

import com.example.appmodule.controller.email.AccountDetailsController;
import com.example.appmodule.dto.email.EmailAccountDto;
import com.example.appmodule.dto.email.EmailMessageDto;
import com.example.appmodule.service.EmailService;
import com.example.appmodule.service.FolderService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EmailDetailsController {
    private final FolderService folderService;
    private final EmailService emailService;
    @FXML
    private Button removeFromFolderButton;

    @FXML
    private Label emailSubjectLabel;

    @FXML
    private Label emailSenderLabel;

    @FXML
    private TextArea emailContentArea;

    @FXML
    private ListView<String> attachmentsListView;

    @FXML
    private Button downloadButton;
    @FXML
    private Button addToFolderButton;

    @FXML
    private Button deleteButton;
    private EmailMessageDto currentEmail;
    private boolean fromMain;
    private EmailAccountDto account;
    private String folder;

    public EmailDetailsController() {
        this.folderService = new FolderService();
        this.emailService = new EmailService();
    }

    public void setFolder(String folder) {
        this.folder = folder;
        if (folder.equals("Inbox") || folder.equals("Trash") || folder.equals("Sent") || folder.equals("Drafts") || folder.equals("All")) {
            removeFromFolderButton.setVisible(false);
        }
        if (!folder.equals("Inbox")) {
            deleteButton.setVisible(false);
        }
    }

    public void setAccount(EmailAccountDto account) {
        this.account = account;
    }

    public void setEmailDetails(EmailMessageDto email, boolean fromMain) {
        currentEmail = email;
        this.fromMain = fromMain;
        emailSubjectLabel.setText("Subject: " + email.getSubject());
        emailSenderLabel.setText("Sender: " + email.getFrom());
        emailContentArea.setText(email.getBody());
        attachmentsListView.getItems().setAll(email.getAttachmentPaths());
        downloadButton.setDisable(email.getAttachmentPaths().isEmpty());
        if (fromMain) {
            addToFolderButton.setVisible(false);
            deleteButton.setVisible(false);
            removeFromFolderButton.setVisible(false);
        }
    }

    @FXML
    private void handleBack() {
        if (fromMain) {
            try {
                //bez css
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/Main.fxml"));
                Parent mainRoot = loader.load();
                Stage stage = (Stage) emailSubjectLabel.getScene().getWindow();
                stage.getScene().setRoot(mainRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/email-account/email-account-page.fxml"));
                BorderPane root = loader.load();

                AccountDetailsController controller = loader.getController();
                controller.setAccount(account);
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                Stage stage = (Stage) emailSubjectLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Email Account Details");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    private void handleDownloadAttachment() {
        String selectedAttachment = attachmentsListView.getSelectionModel().getSelectedItem();
        if (selectedAttachment != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(selectedAttachment);
            File file = fileChooser.showSaveDialog(emailSubjectLabel.getScene().getWindow());

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    // Імітуємо запис файлу
                    writer.write("Файл: " + selectedAttachment + " (зміст файлу)");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            showAlert("Please select an attachment to download.");
        }
    }

    @FXML
    private void handleDeleteEmail() {
        if (currentEmail != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this email?");
            confirmationAlert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                emailService.deleteMessage(account, currentEmail.getMessageId());
                showAlert("Email deleted successfully.");
                handleBack();
            } else {
                showAlert("Deletion cancelled.");
            }
        } else {
            showAlert("No email selected to delete.");
        }
    }

    @FXML
    private void handleRemoveFromFolder() {
        folderService.deleteMessageFromFolder(currentEmail.getMessageId(), account.getEmailAddress(), folder);
        handleBack();
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleAddToFolder() {
        // Створення кастомного діалогу
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Email to Folder");
        dialog.setHeaderText("Select Folder");

        // Кнопка для підтвердження
        ButtonType confirmButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Вміст діалогу: випадаючий список
        ComboBox<String> folderComboBox = new ComboBox<>();
        folderComboBox.setPromptText("Choose a folder");

        // Отримання списку папок через сервіс
        List<String> folderNames = folderService.getFolders(account.getEmailAddress());

        folderNames.removeIf(folder -> folder.equalsIgnoreCase("Inbox") ||
                folder.equalsIgnoreCase("Sent") ||
                folder.equalsIgnoreCase("Drafts") ||
                folder.equalsIgnoreCase("All") ||
                folder.equalsIgnoreCase("Trash"));
        folderComboBox.getItems().addAll(folderNames);

        // Додавання випадаючого списку до діалогу
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(10));
        dialogContent.getChildren().add(folderComboBox);

        dialog.getDialogPane().setContent(dialogContent);

        // Отримання результату
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return folderComboBox.getValue(); // Повертаємо вибране значення
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(folderName -> {
            try {
                // Викликаємо сервіс для додавання повідомлення до папки
                folderService.addMessageToFolder(currentEmail, account.getEmailAddress(), folderName);

                // Показуємо успішне повідомлення
                showAlert(Alert.AlertType.INFORMATION, "Success", "Email has been added to folder: " + folderName);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add email to folder. Please try again.");
            }
        });
    }

    // Метод для показу діалогів (зручний спосіб відображати повідомлення)
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}