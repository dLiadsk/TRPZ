package org.example.service.interpreter;

import org.example.model.EmailMessage;

public interface Expression {
    boolean interpret(EmailMessage email);
}
