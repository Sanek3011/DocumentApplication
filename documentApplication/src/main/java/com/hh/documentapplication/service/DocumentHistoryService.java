package com.hh.documentapplication.service;

import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentAction;
import com.hh.documentapplication.entity.DocumentHistory;
import com.hh.documentapplication.repository.DocumentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentHistoryService {

    private final DocumentHistoryRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAction(DocumentAction action, String initiator, String commentary, Document document) {
        DocumentHistory history = DocumentHistory.builder()
                .documentAction(action)
                .initiator(initiator)
                .commentary(commentary)
                .document(document).build();
        repository.save(history);
    }
}
