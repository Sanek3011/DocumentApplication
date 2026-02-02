package com.hh.documentapplication.entity.dto;

import com.hh.documentapplication.entity.DocumentAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DocumentHistoryDto {

    private String initiator;
    private LocalDateTime actionedAt;
    private DocumentAction documentAction;
    private String commentary;

}
