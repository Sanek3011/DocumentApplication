package com.hh.documentapplication.entity.dto;

import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentHistory;
import com.hh.documentapplication.entity.DocumentStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class DocumentOutDto extends DocumentDto {

    private List<DocumentHistoryDto> historyList;

    public DocumentOutDto(UUID uuid, String author, String title, DocumentStatus status, List<DocumentHistory> historyList) {
        super(uuid, author, title, status);
        this.historyList = prepareHistoryList(historyList);
    }

    public static List<DocumentOutDto> toDtoList(List<Document> documents) {
        return documents.stream()
                .map(document -> new DocumentOutDto(document.getUuid(), document.getAuthor(), document.getTitle(), document.getStatus(), document.getActions()))
                .collect(Collectors.toList());
    }

    private List<DocumentHistoryDto> prepareHistoryList(List<DocumentHistory> historyList) {
        return historyList.stream()
                .map(dh -> new DocumentHistoryDto(dh.getInitiator(), dh.getActionedAt(), dh.getDocumentAction(), dh.getCommentary()))
                .collect(Collectors.toList());
    }
}
