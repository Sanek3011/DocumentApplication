package com.hh.documentapplication.service;

import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j


@Service
public class BatchProcessService {

    private final DocumentService documentService;

    @Transactional
    public Map<String, String> submitBatch(List<String> ids, String initiator) {
        Map<String, String> map = new HashMap<>();
        log.info("[{}] Принято к обработке согласования {} документов",initiator, ids.size());
        Map<String, Document> foundedDocsWithIds = documentService.findDocumentsByIds(ids);
        int success = 0;
        int current = 0;
        for (String id : ids) {
            Document document = foundedDocsWithIds.get(id);
            if (document == null) {
                map.put(id, "Не найден");
                log.warn("[{}] Документ {} не существует. {}/{}",initiator, id, ++current, foundedDocsWithIds.size());
                continue;
            }
            if (!DocumentStatus.DRAFT.equals(document.getStatus())) {
                map.put(id, "Конфликт статуса");
                log.warn("[{}] Документ {} конфликт статуса. {}/{}",initiator, id, ++current, foundedDocsWithIds.size());
                continue;
            }
            try {
                documentService.submitDocument(UUID.fromString(id), null, initiator, document);
                map.put(id, "Успешно согласовано");
                log.info("[{}] Документ {} успешно согласован. {}/{}",initiator, id, ++current, foundedDocsWithIds.size());
                success++;
            }catch (Exception ex) {
                log.error("[{}] Непредвиденная ошибка. Документ не согласован {}, {}/{}",initiator, id, ++current, foundedDocsWithIds.size());
                log.error(ex.getMessage());
            }
        }
        log.info("[{}] Обработано успешно {}/{}",initiator, success, ids.size());
        return map;

    }

    @Transactional
    public Map<String, String> approveBatch(List<String> ids, String initiator) {
        Map<String, String> map = new HashMap<>();
        Map<String, Document> foundedDocsWithIds = documentService.findDocumentsByIds(ids);
        log.info("[{}] Принято к обработке утверждения {} документов",initiator, foundedDocsWithIds.size());
        int success = 0;
        int current = 0;

        for (String id : ids) {
            Document document = foundedDocsWithIds.get(id);
            if (document == null) {
                map.put(id, "Не найден");
                log.warn("[{}] Документ {} не существует. {}/{}", initiator, id, ++current, foundedDocsWithIds.size());
                continue;
            }
            if (!DocumentStatus.SUBMITTED.equals(document.getStatus())) {
                map.put(id, "Конфликт статуса");
                log.warn("[{}] Документ {} конфликт статуса. {}/{}",initiator, document.getUuid(), ++current, foundedDocsWithIds.size());
                continue;
            }
            try {
                documentService.approveDocument(UUID.fromString(id), null, initiator, document);
                map.put(id, "Успешно утверждено");
                log.info("[{}] Документ {} успешно согласован. {}/{}",initiator, document.getUuid(), ++current, foundedDocsWithIds.size());
                success++;
            }catch (Exception ex) {
                map.put(id, "Откат утверждения");
                log.error("[{}] Непредвиденная ошибка. Документ не согласован {}, {}/{}",initiator, id, ++current, foundedDocsWithIds.size());

            }
            log.info("[{}] Обработано успешно {}/{}",initiator, success, ids.size());
        }
        return map;

    }


    /**
     * Перевод UUID в Стрингу для корректного распарсинга и возврата читаемого результата
     */

    private Map<String, Document> prepareFoundMap(List<Document> documents) {
        return documents.stream()
                .collect(Collectors.toMap(doc ->
                        doc.getUuid().toString(), Function.identity()));

    }

}
