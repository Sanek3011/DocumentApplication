package com.hh.documentapplication.aspect;

import com.hh.documentapplication.entity.DocumentAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DocumentChangeStatus {
    DocumentAction value();
}
