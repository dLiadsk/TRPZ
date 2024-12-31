package com.example.appmodule.controller;

import com.example.appmodule.config.AuthenticatedUser;
import com.example.appmodule.config.EmailStatus;
import com.example.appmodule.controller.email.AccountDetailsController;
import com.example.appmodule.controller.email.AddEmailController;
import com.example.appmodule.controller.email_message.EmailDetailsController;
import com.example.appmodule.controller.email_message.WriteMessageController;
import com.example.appmodule.service.EmailAccountService;
import com.example.appmodule.service.EmailService;
import com.example.appmodule.service.FilterService;
import com.example.appmodule.service.UserService;
import com.example.appmodule.dto.email.EmailMessageContextDto;
import com.example.appmodule.dto.email.EmailMessageDto;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import com.example.appmodule.dto.email.EmailAccountDto;
import com.example.appmodule.dto.user.UserDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    private final AuthenticatedUser authenticatedUser;
    private final FilterService filterService;
    private final EmailService emailService;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private TextField senderSearchField;
    @FXML
    private TextField subjectSearchField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label usernameLabel;

    @FXML
    private ListView<EmailAccountDto> emailAccountList;

    @FXML
    private ListView<EmailMessageContextDto> emailsList;

    @FXML
    private Label errorMessage;

    @FXML
    private ComboBox<String> sortCriteriaComboBox;

    private ObservableList<EmailAccountDto> emailAccounts;
    private ObservableList<EmailMessageContextDto> emailMessages;

    public MainController() {
        this.authenticatedUser = AuthenticatedUser.getInstance();
        this.filterService = new FilterService();
        this.emailService = new EmailService();
    }

    @FXML
    public void initialize() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(EmailStatus.values())
                        .map(Enum::name)
                        .collect(Collectors.toList())
        ));
        // Ініціалізація списків
        emailAccounts = FXCollections.observableArrayList();
        emailMessages = FXCollections.observableArrayList();

        emailAccountList.setItems(emailAccounts);
        emailsList.setItems(emailMessages);

        // Завантаження даних користувача
        refreshEmailAccounts();

        // Завантаження повідомлень
        List<EmailMessageContextDto> emails = authenticatedUser.getEmails();
        if (emails != null) {
            emailMessages.setAll(emails);
        }

        // Налаштування обробника подій для списку повідомлень
        emailsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Двічі клацнути на елемент
                EmailMessageContextDto selectedEmail = emailsList.getSelectionModel().getSelectedItem();
                if (selectedEmail != null) {
                    openEmailDetails(selectedEmail);
                }
            }
        });
        emailAccountList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Двічі клацнути на елемент
                EmailAccountDto selectedEmail = emailAccountList.getSelectionModel().getSelectedItem();
                if (selectedEmail != null) {
                    openEmailAccount(selectedEmail);
                }
            }
        });
    }

    private void openEmailAccount(EmailAccountDto emailAccountDto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/email-account/email-account-page.fxml"));
            BorderPane root = loader.load();

            AccountDetailsController controller = loader.getController();
            controller.setAccount(emailAccountDto);

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Email Account Details");
            stage.show();
        } catch (IOException e) {
            showError("Failed to open email details.");
            e.printStackTrace();
        }
    }

    public void refreshEmailAccounts() {
        // Завантаження даних користувача
        UserDto currentUser = authenticatedUser.getUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            emailAccounts.setAll(currentUser.getEmailAccounts());
        }

    }

    @FXML
    private void handleAddEmail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/email-account/add-email-account.fxml"));
            BorderPane root = loader.load();

            AddEmailController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Add Email Account");
            stage.showAndWait();
        } catch (IOException e) {
            showError("Failed to open Add Email Account window.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRefreshEmails() {
        try {
            List<EmailMessageContextDto> emails = emailService.getAllMessages(authenticatedUser.getUser().getEmailAccounts());
            emailMessages.setAll(emails);
            authenticatedUser.setEmails(emails);
        } catch (Exception e) {
            showError("Failed to refresh emails.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/entryPage.fxml"));
            VBox root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(scene);
            HelloController controller = loader.getController();
            controller.setStage(stage);
            authenticatedUser.setUser(null);
            authenticatedUser.setEmails(new ArrayList<>());
            stage.show();
        } catch (IOException e) {
            showError("Failed to quit to entry page.");
            e.printStackTrace();
        }
    }


    public void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);

        // Приховуємо повідомлення через 3 секунди
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                errorMessage.setVisible(false);
            }
        }, 3000);
    }

    private void openEmailDetails(EmailMessageContextDto email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/email-message/view-message.fxml"));
            BorderPane root = loader.load();

            EmailDetailsController controller = loader.getController();
            EmailMessageDto emailDto = emailService.getMessage(email);
            controller.setEmailDetails(emailDto, true);

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Email Details");
            stage.show();
        } catch (IOException e) {
            showError("Failed to open email details.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleWriteMessage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/email-message/create-message.fxml"));
            GridPane root = loader.load();
            WriteMessageController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Write New Mail");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSortEmails() {
        String selectedCriteria = sortCriteriaComboBox.getValue();
        if (selectedCriteria == null) {
            return;
        }

        ObservableList<EmailMessageContextDto> emails = emailsList.getItems();

        switch (selectedCriteria) {
            case "Date" ->
                    FXCollections.sort(emails, Comparator.comparing(EmailMessageContextDto::getSentDate).reversed());
            case "Sender" -> FXCollections.sort(emails, Comparator.comparing(EmailMessageContextDto::getFrom));
            case "Subject" -> FXCollections.sort(emails, Comparator.comparing(EmailMessageContextDto::getSubject));
        }

        // Оновлення списку
        emailsList.setItems(emails);
    }

    @FXML
    private void handleFilterByDate() {
        String selectedStatus = statusFilterComboBox.getValue();
        String senderFilter = senderSearchField.getText();
        String subjectFilter = subjectSearchField.getText().toLowerCase();
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (startDatePicker.getValue() != null) {
            startDate = startDatePicker.getValue().atStartOfDay();
        }
        if (endDatePicker.getValue() != null) {
            endDate = endDatePicker.getValue().atStartOfDay();
        }

        emailMessages.setAll(filterService.filter(authenticatedUser.getEmails(), selectedStatus, senderFilter, subjectFilter, startDate, endDate));
    }
}


