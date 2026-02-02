package com.hh.documentapplication.integration;

import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentStatus;
import com.hh.documentapplication.entity.dto.DocumentDto;
import com.hh.documentapplication.repository.DocumentRepository;
import com.hh.documentapplication.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;


import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;




@SpringBootTest
@ActiveProfiles("test")
public class DocumentIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentsRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Document createDocumentInTransaction() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        return txTemplate.execute(status -> {
            Document doc = Document.builder()
                    .uuid(UUID.randomUUID())
                    .author("Alice")
                    .title("Test Document")
                    .status(DocumentStatus.DRAFT)
                    .build();
            return documentsRepository.save(doc);
        });
    }

    private Document findDocumentInTransaction(UUID uuid) {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        return txTemplate.execute(status ->
                documentsRepository.findByUuid(uuid)
                        .orElseThrow(RuntimeException::new)
        );
    }
    @Test
    void happyPathSingleDocument() {
        Document doc = createDocumentInTransaction();

        documentService.submitDocument(doc.getUuid(), "Комментарий", "Tester", null);
        Document submitted = findDocumentInTransaction(doc.getUuid());
        assertThat(submitted.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
        documentService.approveDocument(submitted.getUuid(), "Одобрено", "Tester", null);

        Document approved = findDocumentInTransaction(doc.getUuid());
        assertThat(approved.getStatus()).isEqualTo(DocumentStatus.APPROVED);
    }
}
