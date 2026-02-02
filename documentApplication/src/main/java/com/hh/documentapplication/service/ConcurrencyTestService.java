package com.hh.documentapplication.service;

import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.exceptions.DocumentStatusConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ConcurrencyTestService {

    private final DocumentService documentService;


    public Map<String, Object> concurrentTest(int threads, int attempts, UUID uuid) throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger conflict = new AtomicInteger(0);
        for (int i = 0; i < attempts; i++) {
            executor.submit(() -> {
                try {
                    documentService.approveDocument(uuid, null, "ConcurrencyTest", null);
                    success.incrementAndGet();
                }catch (DocumentStatusConflictException ex) {
                    conflict.incrementAndGet();
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        executor.close();

        Document document = documentService.getDocumentForConcurrencyTest(uuid);

        Map<String, Object> map = Map.of("Всего попыток", attempts, "Успешных", success.get(), "Конфликтных/неудачных", conflict.get(), "Итоговый статус", document.getStatus());
        return map;

    }
}
