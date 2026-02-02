package com.hh.documentapplication.exceptions;

public class DocumentStatusConflictException extends DocumentToolException {

    public DocumentStatusConflictException() {
        super("конфликт/недопустимая операция");
    }
}
