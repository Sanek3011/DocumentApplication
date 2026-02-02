package com.hh.documentapplication.controller;

import com.hh.documentapplication.entity.DocumentStatus;
import com.hh.documentapplication.entity.dto.DocumentDto;
import com.hh.documentapplication.entity.dto.DocumentOutDto;
import com.hh.documentapplication.service.BatchProcessService;
import com.hh.documentapplication.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@RestController
@RequestMapping("/api/v1/documents")

public class DocumentRestController {

    private final DocumentService documentService;
    private final BatchProcessService batchProcessService;

    @GetMapping("/{Uuid}")
    public ResponseEntity<DocumentOutDto> getDocument(@PathVariable UUID Uuid) {
        DocumentOutDto document = documentService.getDocumentWithHistory(Uuid);
        return ResponseEntity.ok(document);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<DocumentOutDto>> getDocuments(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "createdAt") String sortField,
                                                             @RequestParam(defaultValue = "false") boolean desc,
                                                             @RequestBody List<String> ids) {
        List<DocumentOutDto> listDocumentsWithHistory = documentService.getListDocumentsWithHistory(ids, page, size, sortField, desc);
        return ResponseEntity.ok(listDocumentsWithHistory);
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveDocument(@RequestBody DocumentDto documentDto) {
        String save = documentService.save(documentDto);
        return ResponseEntity.ok(save);
    }

    @PostMapping("/save/batch")
    public ResponseEntity<String> saveDocumentBatch(@RequestBody List<DocumentDto> dtoList) {
        log.info("К созданию принято {} документов", dtoList.size());
        documentService.save(dtoList);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/submit/{uuid}")
    public ResponseEntity<String> submitDocument(@PathVariable UUID uuid,
                                                 @RequestBody(required = false) String commentary,
                                                 @RequestHeader(value = "X-Initiator") String initiator) {
        documentService.submitDocument(uuid, commentary, initiator, null);
        return ResponseEntity.ok("Успешно");
    }

    @PostMapping("/approve/{uuid}")
    public ResponseEntity<String> approveDocument(@PathVariable UUID uuid,
                                                 @RequestBody(required = false) String commentary,
                                                 @RequestHeader(value = "X-Initiator") String initiator) {
        documentService.approveDocument(uuid, commentary, initiator, null);
        return ResponseEntity.ok("Успешно");
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitBatch(@RequestBody List<String> ids,
                                                         @RequestHeader(value = "X-Initiator") String initiator) {
        Map<String, String> resultMap = batchProcessService.submitBatch(ids, initiator);
        return ResponseEntity.ok(resultMap);
    }

    @PostMapping("/approve")
    public ResponseEntity<Map<String, String>> approveBatch(@RequestBody List<String> ids,
                                                           @RequestHeader(value = "X-Initiator") String initiator) {
        Map<String, String> resultMap = batchProcessService.approveBatch(ids, initiator);
        return ResponseEntity.ok(resultMap);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentOutDto>> getDocumentsByFilter(@RequestParam(required = false) DocumentStatus status,
                                                                     @RequestParam(required = false) String author,
                                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                     LocalDateTime createdFrom,
                                                                     @RequestParam(required = false)
                                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                     LocalDateTime createdTo) {
        List<DocumentOutDto> filteredDocuments = documentService.getFilteredDocuments(status, author, createdFrom, createdTo);
        return ResponseEntity.ok(filteredDocuments);

    }

}
