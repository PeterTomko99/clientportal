package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByProjectAndUser(Long projectId, Long userId) {
        return invoiceRepository.findByProjectIdAndProjectUserId(projectId, userId);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceByIdAndUser(Long id, Long userId) {
        return invoiceRepository.findByIdAndProjectUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    @Transactional
    public Invoice save(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Invoice invoice = getInvoiceByIdAndUser(id, userId);
        invoiceRepository.delete(invoice);
    }
}
