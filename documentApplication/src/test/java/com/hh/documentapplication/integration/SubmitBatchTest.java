package com.hh.documentapplication.integration;

import com.hh.documentapplication.aspect.DocumentHistoryAspect;
import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentStatus;
import com.hh.documentapplication.repository.DocumentRepository;
import com.hh.documentapplication.service.BatchProcessService;
import com.hh.documentapplication.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
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
@Transactional
public class SubmitBatchTest {

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
    void batchSubmit() {

        Document doc1 = createDocumentInTransaction(1);
        Document doc2 = createDocumentInTransaction(2);
        Document doc3 = createDocumentInTransaction(3);

        List<String> ids = List.of(doc1.getUuid().toString(), doc2.getUuid().toString(), doc3.getUuid().toString());


        Map<String, String> results = batchProcessService.submitBatch(ids, "Tester");
        verify(documentHistoryAspect, times(1))
                .logHistory(eq(doc1.getUuid()), any(), any(), any());
        verify(documentHistoryAspect, times(1))
                .logHistory(eq(doc2.getUuid()), any(), any(), any());
        verify(documentHistoryAspect, times(1))
                .logHistory(eq(doc3.getUuid()), any(), any(), any());

        doc1 = findDocumentInTransaction(doc1.getUuid());
        doc2 = findDocumentInTransaction(doc2.getUuid());
        doc3 = findDocumentInTransaction(doc3.getUuid());

        assertThat(doc1.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
        assertThat(doc2.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
        assertThat(doc3.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);

        System.out.println("Results map size: " + results.size());
        System.out.println("Results map content: " + results);

        assertThat(results.get(doc1.getUuid().toString())).isEqualTo("Успешно согласовано");
        assertThat(results.get(doc2.getUuid().toString())).isEqualTo("Успешно согласовано");
        assertThat(results.get(doc3.getUuid().toString())).isEqualTo("Успешно согласовано");


    }



    private Document createDocumentInTransaction(Integer i) {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        return txTemplate.execute(status -> {
            Document doc = Document.builder()
                    .uuid(UUID.randomUUID())
                    .author("Alice")
                    .title("Test Document "+i)
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
}
