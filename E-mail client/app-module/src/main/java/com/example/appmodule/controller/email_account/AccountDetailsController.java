package com.example.appmodule.controller.email_account;

import com.example.appmodule.controller.email_message.EmailDetailsController;
import com.example.appmodule.dto.email_account.EmailAccountDto;
import com.example.appmodule.dto.email_message.EmailMessageContextDto;
import com.example.appmodule.dto.email_message.EmailMessageDto;
import com.example.appmodule.service.EmailService;
import com.example.appmodule.service.FolderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static javax.swing.JOptionPane.showInputDialog;


public class AccountDetailsController {
    private final FolderService folderService;
    private final EmailService emailService;

    @FXML
    private Label accountEmailLabel;

    @FXML
    private Label accountDetailsLabel;

    @FXML
    private ListView<String> folderListView;

    @FXML
    private ListView<EmailMessageContextDto> messageListView;

    private EmailAccountDto account;

    public AccountDetailsController() {
        this.folderService = new FolderService();
        this.emailService = new EmailService();

    }

    @FXML
    public void initialize() {
        // Слухач змін вибраної папки
        folderListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadMessagesForFolder(newValue, account.getEmailAddress());
            }
        });

        messageListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Двічі клацнути на елемент
                EmailMessageContextDto selectedEmail = messageListView.getSelectionModel().getSelectedItem();
                if (selectedEmail != null) {
                    openEmailDetails(selectedEmail);

                }
            }
        });
    }

    public void setAccount(EmailAccountDto account) {
        this.account = account;

        // Відображення інформації про акаунт
        accountEmailLabel.setText("Email: " + account.getEmailAddress());

        // Завантаження списку папок
        List<String> folders = loadFoldersForAccount(account);
        folderListView.getItems().setAll(folders);
    }

    private List<String> loadFoldersForAccount(EmailAccountDto account) {
        return folderService.getFolders(account.getEmailAddress());
    }

    private void loadMessagesForFolder(String folderName, String emailAddress) {
        // Імітація завантаження повідомлень для вибраної папки
        ObservableList<EmailMessageContextDto> messages = FXCollections.observableArrayList();
        messages.addAll(folderService.getMessagesFromFolder(folderName, emailAddress));
        messageListView.setItems(messages);
    }

    @FXML
    private void handleBackToMain() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/Main.fxml"));
            Parent mainRoot = loader.load();
            Stage stage = (Stage) accountEmailLabel.getScene().getWindow();
            stage.getScene().setRoot(mainRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void openEmailDetails(EmailMessageContextDto email) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/email-message/view-message.fxml"));
            BorderPane root = loader.load();
            Stage stage = (Stage) accountEmailLabel.getScene().getWindow();
            EmailDetailsController controller = loader.getController();
            EmailMessageDto emailDto = emailService.getMessage(email);
            controller.setEmailDetails(emailDto, false);
            controller.setAccount(account);
            controller.setFolder(String.valueOf(folderListView.getSelectionModel().getSelectedItem()));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Email Details");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateFolder() {
        Optional<String> folderName = Optional.ofNullable(showInputDialog("Create Folder", "Enter folder name:"));
        folderName.ifPresent(name -> {
            List<String> folders = folderService.createFolder(name, account.getEmailAddress());
            folderListView.getItems().setAll(folders);
        });
    }
}
