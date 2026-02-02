package com.hh.documentapplication.service;

import com.hh.documentapplication.entity.ApproveRegistry;
import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.repository.ApproveRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveRegistryService {

    private final ApproveRegistryRepository registryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRegistry(Document document) {
        ApproveRegistry approveRegistry = ApproveRegistry.builder()
                .document(document).build();
        registryRepository.save(approveRegistry);
    }
}
