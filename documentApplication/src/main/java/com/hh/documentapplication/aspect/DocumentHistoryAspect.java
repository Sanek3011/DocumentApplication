package com.hh.documentapplication.aspect;

import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.service.DocumentHistoryService;
import com.hh.documentapplication.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor

@Component
@Aspect
public class DocumentHistoryAspect {

    private final DocumentHistoryService documentHistoryService;
    private final DocumentService documentService;

    @AfterReturning(value = "@annotation(documentChangeStatus) && args(Uuid, commentary,initiator, ..)",
            argNames = "Uuid,commentary,initiator,documentChangeStatus")
    public void logHistory(UUID Uuid,
                           String commentary,
                           String initiator, DocumentChangeStatus documentChangeStatus) {
        Document document = documentService.getDocument(Uuid);
        documentHistoryService.saveAction(documentChangeStatus.value(), initiator, commentary, document);
    }

}
