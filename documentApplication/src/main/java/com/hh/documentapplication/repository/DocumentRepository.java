package com.hh.documentapplication.repository;

import com.hh.documentapplication.entity.Document;
import com.hh.documentapplication.entity.DocumentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Document save(Document document);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Document> findByUuid(UUID uuid);

    @Query(value = """
        select d.uuid
        from documents d
        where d.status = :status
        limit :limit
""", nativeQuery = true)
    List<String> findByStatus(@Param("status") String status, @Param("limit") int limit);


    @Query("""
        select d
        from Document d
        where d.uuid = :uuid
""")
    Optional<Document> findByUuidNoLock(@Param("uuid") UUID uuid);

    @Query("""
        select d
        from Document d
        left join fetch d.actions h
        where d.uuid = :uuid
    """)
    Optional<Document> findByUuidWithHistory(@Param("uuid") UUID uuid);


    @Query("""
    select distinct d
    from Document d
    left join fetch d.actions
    where d.uuid in :uuids
""")
    List<Document> findAllByUuidInWithHistory(@Param("uuids") List<UUID> uuids);

    List<Document> findAllByUuidIn(Collection<UUID> uuids);

    @EntityGraph(attributePaths = "actions")
    Page<Document> findAllByUuidIn(Collection<UUID> uuids, Pageable pageable);


    @Query("""
    select d from Document d
    where (:status is null or d.status = :status)
      and (:author is null or d.author = :author)
      and (cast(:from as timestamp) is null or d.createdAt >= :from)
      and (cast(:to as timestamp) is null or d.createdAt <= :to)
""")
    List<Document> getFilteredDocuments(
            @Param("status") DocumentStatus status,
            @Param("author") String author,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
            );
}
