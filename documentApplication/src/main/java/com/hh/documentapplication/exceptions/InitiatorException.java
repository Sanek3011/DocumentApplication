package com.hh.documentapplication.exceptions;

public class InitiatorException extends DocumentToolException {

    public InitiatorException() {
        super("Представьтесь. Вы не авторизованы");
    }
}
