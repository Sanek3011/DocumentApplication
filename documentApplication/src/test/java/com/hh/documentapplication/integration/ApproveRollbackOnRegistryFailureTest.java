package com.hh.documentapplication.integration;


import com.hh.documentapplication.aspect.DocumentHistoryAspect;
import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentStatus;
import com.hh.documentapplication.repository.DocumentRepository;
import com.hh.documentapplication.service.ApproveRegistryService;
import com.hh.documentapplication.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class ApproveRollbackOnRegistryFailureTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @SpyBean
    private ApproveRegistryService approveRegistryService;

    @SpyBean
    private DocumentHistoryAspect documentHistoryAspect;

    @BeforeEach
    void setUp() {
        // аспект нам не важен — просто чтобы не падал
        doNothing().when(documentHistoryAspect)
                .logHistory(any(), any(), any(), any());
    }

    @Test
    void shouldRollbackApproveWhenRegistrySaveFails() {
        Document doc = createSubmittedDocument();

        doThrow(new RuntimeException("Registry DB error"))
                .when(approveRegistryService)
                .saveRegistry(any());

        assertThatThrownBy(() ->
                documentService.approveDocument(
                        doc.getUuid(),
                        "comment",
                        "Tester",
                        null
                )
        ).isInstanceOf(RuntimeException.class);

        Document reloaded = findDocument(doc.getUuid());

        assertThat(reloaded.getStatus())
                .isEqualTo(DocumentStatus.SUBMITTED);
    }


    private Document createSubmittedDocument() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> {
            Document doc = Document.builder()
                    .uuid(UUID.randomUUID())
                    .author("Alice")
                    .title("Test")
                    .status(DocumentStatus.SUBMITTED)
                    .build();
            return documentRepository.save(doc);
        });
    }

    private Document findDocument(UUID uuid) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status ->
                documentRepository.findByUuid(uuid)
                        .orElseThrow()
        );
    }
}

