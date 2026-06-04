package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void getInvoicesByProjectAndUser_returnsInvoices() {
        List<Invoice> invoices = List.of(new Invoice());
        when(invoiceRepository.findByProjectIdAndProjectUserId(1L, 1L)).thenReturn(invoices);

        List<Invoice> result = invoiceService.getInvoicesByProjectAndUser(1L, 1L);

        assertEquals(invoices, result);
    }

    @Test
    void getInvoiceByIdAndUser_returnsInvoiceWhenFound() {
        Invoice invoice = new Invoice();
        when(invoiceRepository.findByIdAndProjectUserId(1L, 1L)).thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.getInvoiceByIdAndUser(1L, 1L);

        assertEquals(invoice, result);
    }

    @Test
    void getInvoiceByIdAndUser_throwsWhenNotFound() {
        when(invoiceRepository.findByIdAndProjectUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invoiceService.getInvoiceByIdAndUser(1L, 1L));
    }

    @Test
    void delete_deletesInvoiceWhenFound() {
        Invoice invoice = new Invoice();
        when(invoiceRepository.findByIdAndProjectUserId(1L, 1L)).thenReturn(Optional.of(invoice));

        invoiceService.delete(1L, 1L);

        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void delete_throwsWhenInvoiceNotFound() {
        when(invoiceRepository.findByIdAndProjectUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invoiceService.delete(1L, 1L));
    }
}
