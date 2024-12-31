package com.example.appmodule.controller.user;

import com.example.appmodule.config.AuthenticatedUser;
import com.example.appmodule.service.EmailService;
import com.example.appmodule.service.UserService;
import com.example.appmodule.dto.email.EmailMessageContextDto;
import com.example.appmodule.dto.user.UserDto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class UserLoginController {
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final EmailService emailService;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessage;

    public UserLoginController(AuthenticatedUser authenticatedUser, UserService userService, EmailService emailService) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.emailService = emailService;
    }
    public UserLoginController() {
        this.authenticatedUser = AuthenticatedUser.getInstance();
        this.userService = new UserService();
        this.emailService = new EmailService();
    }


    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Перевірка на порожні поля
        if (username.isEmpty() || password.isEmpty()) {
            showError("Both fields must be filled.");
            return;
        }

        try {
            UserDto userDto = userService.login(username, password);

            List<EmailMessageContextDto> emails = emailService.getAllMessages(userDto.getEmailAccounts());
            authenticatedUser.setUser(userDto);
            authenticatedUser.setEmails(emails);
            navigateToMainPage();

        }
        catch (IllegalArgumentException ex){
            showError("Invalid username or password.");
        }
    }

    @FXML
    private void handleCreateAccount() {
        try {
            // Завантажуємо FXML файл для сторінки реєстрації
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/user/create-user.fxml"));
            AnchorPane root = loader.load();

            // Створюємо нову сцену для сторінки реєстрації
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow(); // отримуємо поточне вікно
            stage.setScene(scene); // змінюємо сцену на сторінку реєстрації

            // Покажемо нову сцену
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load the registration page.");
        }
    }
    private void navigateToMainPage() {
        try {
            // Завантажуємо FXML файл для головної сторінки
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/main.fxml"));
            BorderPane root = loader.load();

            // Створюємо нову сцену для головної сторінки
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow(); // отримуємо поточне вікно
            stage.setScene(scene); // змінюємо сцену на головну сторінку

            // Покажемо нову сцену
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load the main page.");
        }
    }
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
}
