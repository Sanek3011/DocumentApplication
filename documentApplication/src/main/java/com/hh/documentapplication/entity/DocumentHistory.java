package com.hh.documentapplication.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "document")

@Entity
@Table(name = "documents_history")
public class DocumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "initiator", nullable = false)
    private String initiator;
    @CreationTimestamp
    private LocalDateTime actionedAt;
    @Enumerated(EnumType.STRING)
    private DocumentAction documentAction;
    @Column(name = "commentary")
    private String commentary;
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

}
