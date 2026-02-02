package com.hh.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentDto {

    private UUID uuid;
    private String author;
    private String title;
    private DocumentStatus status;




    
}
