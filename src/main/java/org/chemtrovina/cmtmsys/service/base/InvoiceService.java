package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.dto.InvoiceDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InvoiceService {

    void add(Invoice invoice);
    void update(Invoice invoice);
    Invoice findById(int id);
    List<Invoice> findAll();
    Invoice findByInvoiceNo(String invoiceNo);

    void saveInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details);

    boolean existsByInvoiceNo(String invoiceNo);

    List<InvoiceDetail> getInvoiceDetails(String invoiceNo);
    void updateInvoiceDetails(String invoiceNo, List<InvoiceDetail> details);

    void deleteInvoiceDetail(int invoiceId, String sapPN);

    //List<Invoice> findByDateAndInvoiceNo(LocalDate date, String invoiceNo);

    List<Invoice> getInvoicesByDate(LocalDate date);
    List<Invoice> getInvoicesByInvoiceNo(String invoiceNo);
    Invoice getInvoicesByInvoicePN(String invoicePN);
    Invoice getInvoiceByInvoiceNo(String invoiceNo);
    List<Invoice> getInvoicesByDateAndInvoiceNo(LocalDate date, String invoiceNo);
    List<String> getAllInvoiceNos();
    List<String> getAllInvoicePNs();

    List<InvoiceDetail> getInvoiceDetailsByInvoiceId(int invoiceId);

    void deleteInvoice(int invoiceId);
    int countHistoryByInvoiceId(int invoiceId);

    List<InvoiceDetailViewDto> searchByFields(String invoicePN, String sapPN, String makerPN, LocalDate date);






}
