package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;

import java.util.List;

public interface InvoiceService {

    void add(Invoice invoice);
    void update(Invoice invoice);
    Invoice findById(int id);
    List<Invoice> findAll();
    Invoice findByInvoiceNo(String invoiceNo);

    void saveInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details);

    boolean existsByInvoiceNo(String invoiceNo);

}
