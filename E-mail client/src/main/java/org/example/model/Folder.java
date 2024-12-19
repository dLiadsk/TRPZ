package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Folder {
    private Long id;
    private String name;  // Назва папки
    private List<EmailMessage> messages;  // Список повідомлень у папці

    // Пошук повідомлень за темою
    public List<EmailMessage> searchBySubject(String subject) {
        List<EmailMessage> foundMessages = new ArrayList<>();
        for (EmailMessage message : messages) {
            if (message.getSubject().contains(subject)) {
                foundMessages.add(message);
            }
        }
        return foundMessages;
    }

    // Перевірка наявності повідомлень у папці
    public boolean hasMessages() {
        return !messages.isEmpty();
    }

}