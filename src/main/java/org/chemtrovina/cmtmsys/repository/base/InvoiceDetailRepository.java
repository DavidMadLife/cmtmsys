package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;

import java.util.List;

public interface InvoiceDetailRepository extends GenericRepository<InvoiceDetail> {
    List<InvoiceDetail> findByInvoiceId(String invoiceId);

    InvoiceDetail findById(int id);
    List<InvoiceDetail> findAll();
    void updateInvoiceDetail(int detailId, InvoiceDetail newDetail);
    InvoiceDetail findBySapPNAndInvoiceId(String sapPN, int invoiceId);

    List<String> findAllSapPNs();

}
