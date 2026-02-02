package com.hh.documentapplication.service;

import com.hh.documentapplication.aspect.DocumentChangeStatus;
import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentAction;
import com.hh.documentapplication.entity.DocumentStatus;
import com.hh.documentapplication.entity.dto.DocumentDto;
import com.hh.documentapplication.entity.dto.DocumentOutDto;
import com.hh.documentapplication.exceptions.DocumentNotFoundException;
import com.hh.documentapplication.exceptions.DocumentStatusConflictException;
import com.hh.documentapplication.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j

@Service
public class DocumentService {

    private final DocumentRepository documentsRepository;
    private final ApproveRegistryService approveRegistryService;

    public String save(DocumentDto documentDto) {
        Document document = Document.builder()
                .author(documentDto.getAuthor())
                .title(documentDto.getTitle())
                .status(DocumentStatus.DRAFT).build();
        try {
            Document save = documentsRepository.save(document);
            log.info("Успешно сохранен документ {}",save.getUuid());
            return save.getUuid().toString();
        }catch (Exception ex) {
            log.error("Документ {} не сохранен по причине {}", document.getTitle(), ex.getMessage() );
        }
    }

    /**
     * использование save вместо saveAll
     * для наглядного отражения хода выполнения сохранения пачки
     */

    public void save(List<DocumentDto> documentDtos) {
        int success = 0;
        for (int i = 0; i < documentDtos.size(); i++) {
            DocumentDto d = documentDtos.get(i);
            Document document = Document.builder()
                    .author(d.getAuthor())
                    .title(d.getTitle())
                    .status(DocumentStatus.DRAFT).build();
            try {
                Document save = documentsRepository.save(document);
                log.info("Успешно сохранен документ {}, {} из {}",save.getUuid(), ++success, documentDtos.size());
            }catch (Exception ex) {
                log.error("Документ {} не сохранен по причине {}", document.getTitle(), ex.getMessage() );
            }
        }
        log.info("Создано {}/{} документов", success, documentDtos.size());
    }

    public DocumentOutDto getDocumentWithHistory(UUID uuid) {
        Document document = documentsRepository.findByUuidWithHistory(uuid).orElseThrow(DocumentNotFoundException::new);
        return new DocumentOutDto(document.getUuid(), document.getAuthor(), document.getTitle(), document.getStatus(), document.getActions());
    }



    public List<DocumentOutDto> getListDocumentsWithHistory(List<String> ids, int page, int size, String sortField, boolean descending) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(getDirection(descending), sortField));
        List<UUID> collect = prepareIds(ids);
        Page<Document> allByUuidIn = documentsRepository.findAllByUuidIn(collect, pageable);
        return DocumentOutDto.toDtoList(allByUuidIn.getContent());

    }

    private Sort.Direction getDirection(boolean descending) {
        return descending ? Sort.Direction.DESC : Sort.Direction.ASC;
    }


    public Document getDocument(UUID Uuid) {
        return documentsRepository.findByUuid(Uuid).orElseThrow(DocumentNotFoundException::new);
    }


    /**
     * @param commentary используется в аспекте
     * @param initiator  используется в аспекте
     */

    @DocumentChangeStatus(DocumentAction.SUBMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitDocument(UUID Uuid, String commentary, String initiator, Document document) {
        if (document == null) {
            document = documentsRepository.findByUuid(Uuid).orElseThrow(DocumentNotFoundException::new);
        }
        if (!DocumentStatus.DRAFT.equals(document.getStatus())) {
            throw new DocumentStatusConflictException();
        }
        document.setStatus(DocumentStatus.SUBMITTED);
        documentsRepository.save(document);
    }


    public Map<String, Document> findDocumentsByIds(List<String> ids) {
        List<UUID> collect = prepareIds(ids);
        return prepareFoundMap(documentsRepository.findAllByUuidIn(collect));

    }
    /**
     * Перевод UUID в Стрингу для корректного распарсинга и возврата причины
     */

    private Map<String, Document> prepareFoundMap(List<Document> documents) {
        return documents.stream()
                .collect(Collectors.toMap(doc ->
                        doc.getUuid().toString(), Function.identity()));

    }

    private List<UUID> prepareIds(List<String> ids) {
        return ids.stream()
                .map(id -> {
                    try {
                        return UUID.fromString(id);
                    } catch (IllegalArgumentException ex) {
                        log.warn("id {} не соответствует формату", id);
                        return null;
                    }
                })
                .toList();
    }

    /**
     * @param commentary используется в аспекте
     * @param initiator  используется в аспекте
     */

    @DocumentChangeStatus(DocumentAction.APPROVE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approveDocument(UUID Uuid, String commentary, String initiator, Document document) {
        if (document == null) {
            document = documentsRepository.findByUuid(Uuid).orElseThrow(DocumentNotFoundException::new);
        }
        if (!DocumentStatus.SUBMITTED.equals(document.getStatus())) {
            throw new DocumentStatusConflictException();
        }
        document.setStatus(DocumentStatus.APPROVED);
        documentsRepository.saveAndFlush(document); // Моё мнение можно обойтись try catch, используя одну транзакцию и propogation.MANDATORY, но следую требованиям ТЗ
        try {
            approveRegistryService.saveRegistry(document);
        }catch (Exception ex) {
            document.setStatus(DocumentStatus.SUBMITTED);
            documentsRepository.save(document);
            log.warn("Ошибка при записи в регистр. Документ: {}. Текущий статус: {} ", document.getUuid(), document.getStatus());
            throw new RuntimeException();
        }

    }

    public List<DocumentOutDto> getFilteredDocuments(DocumentStatus status, String author, LocalDateTime createdFrom, LocalDateTime createdTo) {
        List<Document> filteredDocuments = documentsRepository.getFilteredDocuments(status, author, createdFrom, createdTo);
        return DocumentOutDto.toDtoList(filteredDocuments);
    }

    public Document getDocumentForConcurrencyTest(UUID uuid) {
        return documentsRepository.findByUuidNoLock(uuid).orElseThrow(DocumentNotFoundException::new);
    }

    public List<String> findUUIDsByStatus(DocumentStatus status, int batchSize) {
        return documentsRepository.findByStatus(status.toString(), batchSize);
    }

}
