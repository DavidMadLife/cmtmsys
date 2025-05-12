package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.repository.base.InvoiceDetailRepository;
import org.chemtrovina.cmtmsys.service.base.InvoiceDetailService;

import java.util.List;

public class InvoiceDetailServiceImpl implements InvoiceDetailService {

    private final InvoiceDetailRepository invoiceDetailRepository;

    public InvoiceDetailServiceImpl(InvoiceDetailRepository invoiceDetailRepository) {
        this.invoiceDetailRepository = invoiceDetailRepository;
    }

    @Override
    public void add(InvoiceDetail invoiceDetail) {
        invoiceDetailRepository.add(invoiceDetail);
    }

    @Override
    public void update(InvoiceDetail invoiceDetail) {
        invoiceDetailRepository.update(invoiceDetail);
    }

    @Override
    public InvoiceDetail findById(int id) {
        return invoiceDetailRepository.findById(id);
    }

    @Override
    public List<InvoiceDetail> findAll() {
        return invoiceDetailRepository.findAll();
    }

    @Override
    public List<InvoiceDetail> findByInvoiceId(String invoiceId) {
        return invoiceDetailRepository.findByInvoiceId(invoiceId);
    }

    @Override
    public InvoiceDetail getInvoiceDetailBySapPNAndInvoiceId(String sapPN, int invoiceId) {
        return invoiceDetailRepository.findBySapPNAndInvoiceId(sapPN, invoiceId);
    }

}
