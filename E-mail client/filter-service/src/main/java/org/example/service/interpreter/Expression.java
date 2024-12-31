package org.example.service.interpreter;

import org.example.dto.EmailMessageContextDto;

public interface Expression {
    boolean interpret(EmailMessageContextDto email);
}
