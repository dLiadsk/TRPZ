package com.example.appmodule.controller.user;

import com.example.appmodule.config.AuthenticatedUser;
import com.example.appmodule.controller.MainController;
import com.example.appmodule.service.UserService;
import com.example.appmodule.dto.user.UserDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class UserCreateController {
    private final AuthenticatedUser authenticatedUser;

    private final UserService userService;

    public UserCreateController() {
        this.authenticatedUser = AuthenticatedUser.getInstance();
        this.userService = new UserService(); // Default initialization
    }
    public UserCreateController(AuthenticatedUser authenticatedUser, UserService userService) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
    }

    @FXML
    private TextField usernameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorMessage;

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        String username = usernameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Перевірка на порожні поля
        if (username.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields must be filled.");
            return;
        }

        // Перевірка на правильність телефону
        if (!isPhoneNumberValid(phoneNumber)) {
            showError("Invalid phone number.");
            return;
        }

        // Перевірка на збіг паролів
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        // Перевірка на складність пароля (наприклад, мінімум 8 символів)
        if (password.length() < 8) {
            showError("Password must be at least 8 characters.");
            return;
        }
        try {
            UserDto user = userService.singUp(username, password, phoneNumber);
            authenticatedUser.setUser(user);
            showError("Account created successfully!");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/main.fxml"));
            BorderPane root = loader.load();

            MainController controller = loader.getController();

            controller.showError("User created successfully");


            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);

            stage.show();
        }
        catch (IllegalArgumentException ex){
            showError("Account wasn't be created");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load the main page.");
        }

    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        String regex = "^\\+?[0-9]{10,13}$";  // Простий приклад для міжнародного номеру
        return phoneNumber.matches(regex);
    }


    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);

        // Приховуємо повідомлення через 3 секунди
        if (!message.equals("Account created successfully!")) {
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    errorMessage.setVisible(false);
                }
            }, 3000);
        }
    }
    @FXML
    private void handleLogin() {
        try {
            // Завантажуємо FXML файл для сторінки реєстрації
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/appmodule/user/login-user.fxml"));
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
}
