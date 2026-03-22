package com.example.demo.shared.exception;

public class BusinessRuleException extends DomainException {

    private final String rule;

    public BusinessRuleException(String message) {
        super(message);
        this.rule = null;
    }

    public BusinessRuleException(String rule, String message) {
        super(message);
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}