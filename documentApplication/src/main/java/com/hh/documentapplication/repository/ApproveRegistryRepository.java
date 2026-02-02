package com.hh.documentapplication.repository;

import com.hh.documentapplication.entity.ApproveRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproveRegistryRepository extends JpaRepository<ApproveRegistry, Long> {
}
