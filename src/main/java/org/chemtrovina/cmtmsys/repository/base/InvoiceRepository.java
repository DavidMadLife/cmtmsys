package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Invoice;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends GenericRepository<Invoice> {
    Invoice findByInvoiceNo(String invoiceNo);
    List<Invoice> search(String invoiceNo, LocalDate invoiceDate, String status);
}
