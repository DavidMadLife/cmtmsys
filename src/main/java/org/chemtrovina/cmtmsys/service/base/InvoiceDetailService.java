package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.InvoiceDetail;

import java.util.List;

public interface InvoiceDetailService {

    void add(InvoiceDetail invoiceDetail);
    void update(InvoiceDetail invoiceDetail);
    InvoiceDetail findById(int id);
    List<InvoiceDetail> findAll();
    List<InvoiceDetail> findByInvoiceId(String invoiceId);
}
