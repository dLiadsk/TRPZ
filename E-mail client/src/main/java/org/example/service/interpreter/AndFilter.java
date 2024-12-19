package org.example.service.interpreter;

import org.example.model.EmailMessage;

public class AndFilter implements Expression {
    private Expression filter1;
    private Expression filter2;

    public AndFilter(Expression filter1, Expression filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    @Override
    public boolean interpret(EmailMessage emailMessage) {
        return filter1.interpret(emailMessage) && filter2.interpret(emailMessage);
    }
}
