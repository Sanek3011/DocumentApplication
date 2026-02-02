package com.hh.documentapplication.converter;

import com.hh.documentapplication.entity.DocumentStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class DocumentStatusConverter implements Converter<String, DocumentStatus> {

    @Nullable
    @Override
    public DocumentStatus convert(String source) {
        try {
            return DocumentStatus.valueOf(source.toUpperCase());
        }catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
