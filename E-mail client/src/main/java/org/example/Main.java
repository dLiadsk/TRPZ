package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.EmailAccount;
import org.example.model.EmailMessage;
import org.example.model.User;
import org.example.model.common.EmailStatus;
import org.example.model.decorator.EmailFilter;
import org.example.repository.EmailAccountRepository;
import org.example.repository.UserRepository;
import org.example.service.EmailAccountService;
import org.example.service.EmailFilterService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Main extends Application {



    public static void main(String[] args) throws SQLException {
//        DbConnection dbConnection = DbConnection.getInstance();
//        launch();
//        testEmailAccount();
        List<EmailMessage> emailMessages = new EmailFilterService()
                .emailUnreadMessageDateFilter(emailMessages(), LocalDate.of(2023, 11, 1), LocalDate.of(2023, 12, 31));
        emailMessages.forEach(System.out::println);
    }

    public static void testEmailAccount(){
        EmailAccountService emailAccountService = new EmailAccountService(new EmailAccountRepository(), new UserRepository());
//        try {
//            emailAccountService.addEmailAccount(new User(), new EmailAccount("qqwe35949@gmail.com", "Asdfgh321"));
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        EmailAccount emailAccount = new EmailAccount.EmailAccountBuilder("qwertyeee@ukr.net", "O4oiRBZT1bj5V8my").setAutoconfig(true).build();
        emailAccountService.authorizeEmail(emailAccount);

    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("mainScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    static List<EmailMessage> emailMessages() {
        // Створюємо список EmailMessage
        List<EmailMessage> emailMessages = new ArrayList<>();

        // Додаємо тестові повідомлення
        EmailMessage email1 = new EmailMessage();
        email1.setId("1");
        email1.setSubject("Робоча зустріч");
        email1.setFrom("manager@example.com");
        email1.setTo(List.of("employee1@example.com", "employee2@example.com"));
        email1.setSentDate(LocalDate.of(2023, 12, 1));
        email1.setBody("Доброго дня! Нагадую про зустріч у понеділок о 10:00.");
        email1.setEmailStatus(EmailStatus.UNREAD); // Коректно встановлюємо статус
        email1.setAttachments(List.of()); // Немає вкладень

        EmailMessage email2 = new EmailMessage();
        email2.setId("2");
        email2.setSubject("Звіт за проектом");
        email2.setFrom("teamlead@example.com");
        email2.setTo(List.of("manager@example.com"));
        email2.setSentDate(LocalDate.of(2023, 12, 13));
        email2.setBody("Добрий день! Надсилаю звіт про стан проекту.");
        email2.setEmailStatus(EmailStatus.UNREAD); // Встановлюємо статус
        email2.setAttachments(List.of()); // Немає вкладень

        EmailMessage email3 = new EmailMessage();
        email3.setId("3");
        email3.setSubject("Документи");
        email3.setFrom("hr@example.com");
        email3.setTo(List.of("new.employee@example.com"));
        email3.setSentDate(LocalDate.of(2023, 12, 8));
        email3.setBody("Вітаю! Надсилаю документи для ознайомлення.");
        email3.setEmailStatus(EmailStatus.UNREAD); // Встановлюємо статус
        email3.setAttachments(List.of()); // Немає вкладень

        // Додаємо повідомлення у список
        emailMessages.add(email1);
        emailMessages.add(email2);
        emailMessages.add(email3);
        return emailMessages;
    }

}