package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.InvoiceDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends GenericRepository<Invoice> {
    Invoice findByInvoiceNo(String invoiceNo);
    Invoice findByInvoicePN(String invoicePN);

    List<Invoice> search(String invoiceNo, LocalDate invoiceDate, String status);
    void saveInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details);
    boolean existsByInvoiceNo(String invoiceNo);
    List<InvoiceDetail> getInvoiceDetails(String invoiceNo);
    void updateInvoiceDetails(String invoiceNo, List<InvoiceDetail> details);
    void deleteInvoiceDetail(int invoiceId, String sapPN);

    List<Invoice> findInvoicesByDate(LocalDate date);
    List<Invoice> findInvoicesByInvoiceNo(String invoiceNo);
    Invoice findInvoicesByInvoicePN(String invoicePN);
    Invoice findInvoiceByInvoiceNo(String invoicePN);
    List<Invoice> findInvoicesByDateAndInvoiceNo(LocalDate date, String invoiceNo);
    List<String> findAllInvoiceNos();
    List<String> findAllInvoicePNs();

    Invoice findInvoiceById(int invoiceId);
    List<InvoiceDetail> getInvoiceDetailsByInvoiceId(int invoiceId);

    void deleteInvoice(int invoiceId);
    int countHistoryByInvoiceId(int invoiceId);

    List<InvoiceDetailViewDto> advancedSearch(String invoicePN, String sapPN, String makerPN, LocalDate date);









}
