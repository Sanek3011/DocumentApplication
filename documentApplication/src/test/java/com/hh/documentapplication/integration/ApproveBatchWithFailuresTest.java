package com.hh.documentapplication.integration;

import com.hh.documentapplication.aspect.DocumentHistoryAspect;
import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentStatus;
import com.hh.documentapplication.repository.DocumentRepository;
import com.hh.documentapplication.service.BatchProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class ApproveBatchWithFailuresTest {

    @Autowired
    private DocumentRepository documentsRepository;

    @Autowired
    private BatchProcessService batchProcessService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @SpyBean
    private DocumentHistoryAspect documentHistoryAspect;


    @BeforeEach
    void setUp() {
        // Проверяем чисто вызов аспекта
        doNothing().when(documentHistoryAspect)
                .logHistory(any(), any(), any(), any());
    }

    @Test
    public void submitBatchWithFailures() {
        Document doc1 = createDocumentInTransaction(1, false);
        Document doc2 = createDocumentInTransaction(2, true);
        Document doc3 = createDocumentInTransaction(3, false);

        List<String> ids = List.of(doc1.getUuid().toString(), doc2.getUuid().toString(), doc3.getUuid().toString());


        Map<String, String> results = batchProcessService.approveBatch(ids, "Tester");
        verify(documentHistoryAspect, times(1))
                .logHistory(eq(doc1.getUuid()), any(), any(), any());
        verify(documentHistoryAspect, never())
                .logHistory(eq(doc2.getUuid()), any(), any(), any());
        verify(documentHistoryAspect, times(1))
                .logHistory(eq(doc3.getUuid()), any(), any(), any());

        doc1 = findDocumentInTransaction(doc1.getUuid());
        doc2 = findDocumentInTransaction(doc2.getUuid());
        doc3 = findDocumentInTransaction(doc3.getUuid());

        assertThat(doc1.getStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(doc2.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(doc3.getStatus()).isEqualTo(DocumentStatus.APPROVED);

        System.out.println("Results map size: " + results.size());
        System.out.println("Results map content: " + results);

        assertThat(results.get(doc1.getUuid().toString())).isEqualTo("Успешно утверждено");
        assertThat(results.get(doc2.getUuid().toString())).isEqualTo("Конфликт статуса");
        assertThat(results.get(doc3.getUuid().toString())).isEqualTo("Успешно утверждено");

    }


    private Document createDocumentInTransaction(Integer i, boolean isDraft) {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        return txTemplate.execute(status -> {
            Document doc = Document.builder()
                    .uuid(UUID.randomUUID())
                    .author("Alice")
                    .title("Test Document "+i)
                    .build();
            if (isDraft) {
                doc.setStatus(DocumentStatus.DRAFT);
            }else{
                doc.setStatus(DocumentStatus.SUBMITTED);
            }
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
}
