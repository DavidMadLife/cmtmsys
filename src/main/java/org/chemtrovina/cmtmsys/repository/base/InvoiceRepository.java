package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends GenericRepository<Invoice> {
    Invoice findByInvoiceNo(String invoiceNo);
    List<Invoice> search(String invoiceNo, LocalDate invoiceDate, String status);
    void saveInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details);
    boolean existsByInvoiceNo(String invoiceNo);
}
