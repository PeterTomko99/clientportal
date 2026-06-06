package com.PeterTomko.clientportal.repository;

import com.PeterTomko.clientportal.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByProjectId(Long projectId);

    List<Invoice> findByProjectIdAndProjectUserId(Long projectId, Long userId);

    Page<Invoice> findByProjectIdAndProjectUserId(Long projectId, Long userId, Pageable pageable);

    Optional<Invoice> findByIdAndProjectUserId(Long id, Long userId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.project p JOIN FETCH p.user WHERE i.status = :status AND i.dueDate < :date")
    List<Invoice> findOverdueWithDetails(@Param("status") Invoice.Status status, @Param("date") LocalDate date);
}
