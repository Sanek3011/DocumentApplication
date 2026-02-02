package com.hh.documentapplication.service;

import com.hh.documentapplication.entity.DocumentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j

@Service
public class WorkersService{

    private final DocumentService documentService;
    private final BatchProcessService batchProcessService;
    private final Integer batchSize;
    private final String SUBMIT_WORKER_NAME = "SUBMIT-worker";
    private final String APPROVE_WORKER_NAME = "APPROVE-worker";

    public WorkersService(@Value("${properties.workers.batchsize}") Integer batchSize, DocumentService documentService, BatchProcessService batchProcessService) {
        this.batchSize = batchSize;
        this.documentService = documentService;
        this.batchProcessService = batchProcessService;
    }

    public void submitWorker() {
        try {
            List<String> ids = getDocsIds(DocumentStatus.DRAFT);
            if (!ids.isEmpty()) {
            log.info("{}: {} отправка пачки размером {} на согласование", LocalDateTime.now(), SUBMIT_WORKER_NAME, ids.size());
            batchProcessService.submitBatch(ids, SUBMIT_WORKER_NAME);
            }
        } catch (Exception ex) {
            log.error("Ошибка при обработки пачки согласования {}", SUBMIT_WORKER_NAME);
            ex.printStackTrace();;
        }
    }

    public void approveWorker() {
        try {
            List<String> ids = getDocsIds(DocumentStatus.SUBMITTED);
            if (!ids.isEmpty()) {
                log.info("{}: {} отправка пачки размером {} на утверждение", LocalDateTime.now(), SUBMIT_WORKER_NAME, ids.size());
                batchProcessService.approveBatch(ids, APPROVE_WORKER_NAME);
            }
        }catch (Exception ex) {
            log.error("Ошибка при обработки пачки утверждения {}", APPROVE_WORKER_NAME);
            ex.printStackTrace();
        }

    }

    private List<String> getDocsIds(DocumentStatus status) {
        return documentService.findUUIDsByStatus(status, batchSize);
    }

}
