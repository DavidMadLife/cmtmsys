package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.chemtrovina.cmtmsys.service.base.InvoiceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;



    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> getInvoicesByDate(LocalDate date) {
        return invoiceRepository.findInvoicesByDate(date);
    }

    public List<Invoice> getInvoicesByInvoiceNo(String invoiceNo) {
        return invoiceRepository.findInvoicesByInvoiceNo(invoiceNo);
    }

    public List<Invoice> getInvoicesByDateAndInvoiceNo(LocalDate date, String invoiceNo) {
        return invoiceRepository.findInvoicesByDateAndInvoiceNo(date, invoiceNo);
    }

    public List<String> getAllInvoiceNos() {
        return invoiceRepository.findAllInvoiceNos();
    }

    @Override
    public List<InvoiceDetail> getInvoiceDetailsByInvoiceId(int invoiceId) {
        return invoiceRepository.getInvoiceDetailsByInvoiceId(invoiceId);
    }



    @Override
    public void updateInvoiceDetails(String invoiceNo, List<InvoiceDetail> details) {
        invoiceRepository.updateInvoiceDetails(invoiceNo, details);
    }

    @Override
    public void deleteInvoiceDetail(int invoiceId, String sapPN) {
        invoiceRepository.deleteInvoiceDetail(invoiceId, sapPN);
    }




    @Override
    public void add(Invoice invoice) {
        invoiceRepository.add(invoice);
    }

    @Override
    public void update(Invoice invoice) {
        invoiceRepository.update(invoice);
    }

    @Override
    public Invoice findById(int id) {
        return invoiceRepository.findById(id);
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    @Override
    public Invoice findByInvoiceNo(String invoiceNo) {
        return invoiceRepository.findByInvoiceNo(invoiceNo);
    }

    @Override
    public void saveInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details) {
        invoiceRepository.saveInvoiceWithDetails(invoice, details);
    }

    @Override
    public boolean existsByInvoiceNo(String invoiceNo) {
        invoiceRepository.existsByInvoiceNo(invoiceNo);
        return true;
    }

    @Override
    public List<InvoiceDetail> getInvoiceDetails(String invoiceNo) {
        return invoiceRepository.getInvoiceDetails(invoiceNo);
    }

    @Override
    public void deleteInvoice(int invoiceId) {
        invoiceRepository.deleteInvoice(invoiceId);
    }

    @Override
    public int countHistoryByInvoiceId(int invoiceId) {
        return invoiceRepository.countHistoryByInvoiceId(invoiceId);
    }



}
