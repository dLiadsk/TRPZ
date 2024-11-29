package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.EmailAccount;
import org.example.model.User;
import org.example.repository.EmailAccountRepository;
import org.example.repository.UserRepository;
import org.example.service.EmailAccountService;

import java.sql.SQLException;


public class Main extends Application {



    public static void main(String[] args) {
//        DbConnection dbConnection = DbConnection.getInstance();
//        launch();
        testEmailAccount();
    }

    public static void testEmailAccount(){
        EmailAccountService emailAccountService = new EmailAccountService(new EmailAccountRepository(), new UserRepository());
//        try {
//            emailAccountService.addEmailAccount(new User(), new EmailAccount("qqwe35949@gmail.com", "Asdfgh321"));
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        try {
            emailAccountService.addEmailAccount(new User(), new EmailAccount("qwertyeee@ukr.net", "O4oiRBZT1bj5V8my"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("mainScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}