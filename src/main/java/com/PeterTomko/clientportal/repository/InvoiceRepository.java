package com.PeterTomko.clientportal.repository;

import com.PeterTomko.clientportal.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByProjectId(Long projectId);

    List<Invoice> findByProjectIdAndProjectUserId(Long projectId, Long userId);

    Optional<Invoice> findByIdAndProjectUserId(Long id, Long userId);
}
