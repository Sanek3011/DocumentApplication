package com.hh.documentapplication.exceptions;

public class DocumentNotFoundException extends DocumentToolException {

    public DocumentNotFoundException() {
        super("Не найдено");
    }
}
