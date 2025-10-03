package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Solder;
import org.chemtrovina.cmtmsys.repository.base.SolderRepository;
import org.chemtrovina.cmtmsys.service.base.SolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SolderServiceImpl implements SolderService {

    private final SolderRepository solderRepository;

    public SolderServiceImpl(SolderRepository solderRepository) {
        this.solderRepository = solderRepository;
    }

    // =======================
    // CRUD
    // =======================
    @Override
    @Transactional
    public void addSolder(Solder solder) {
        validateDates(solder);
        if (solderRepository.existsByCode(solder.getCode())) {
            throw new IllegalArgumentException("Code đã tồn tại: " + solder.getCode());
        }
        solderRepository.add(solder);
    }

    // SolderServiceImpl.java
    @Override
    @Transactional
    public void updateSolder(Solder solder) {
        validateDates(solder);

        // NGĂN TRÙNG CODE: nếu có bản ghi khác dùng code này -> báo lỗi
        Solder existed = solderRepository.findByCode(solder.getCode());
        if (existed != null && existed.getSolderId() != solder.getSolderId()) {
            throw new IllegalArgumentException("Code đã tồn tại: " + solder.getCode());
        }

        solderRepository.update(solder);
    }


    @Override
    @Transactional
    public void deleteSolderById(int solderId) {
        solderRepository.deleteById(solderId);
    }

    // =======================
    // Queries
    // =======================
    @Override
    public Solder getSolderById(int solderId) {
        return solderRepository.findById(solderId);
    }

    @Override
    public Solder getSolderByCode(String code) {
        return solderRepository.findByCode(code);
    }

    @Override
    public List<Solder> getAllSolders() {
        return solderRepository.findAll();
    }

    // =======================
    // Helpers
    // =======================
    @Override
    public boolean existsByCode(String code) {
        return solderRepository.existsByCode(code);
    }

    @Override
    public List<Solder> search(String code,
                               String maker,
                               String lot,
                               LocalDate receivedFrom,
                               LocalDate receivedTo,
                               LocalDate expiryFrom,
                               LocalDate expiryTo) {
        return solderRepository.search(code, maker, lot, receivedFrom, receivedTo, expiryFrom, expiryTo);
    }

    @Override
    public List<Solder> findExpiringBetween(LocalDate from, LocalDate to) {
        return solderRepository.findExpiringBetween(from, to);
    }

    // =======================
    // Private validations
    // =======================
    private void validateDates(Solder s) {
        LocalDate mfg = s.getMfgDate();
        LocalDate exp = s.getExpiryDate();
        LocalDate recv = s.getReceivedDate();

        if (mfg != null && exp != null && exp.isBefore(mfg)) {
            throw new IllegalArgumentException("ExpiryDate phải >= MfgDate");
        }
        if (mfg != null && recv != null && recv.isBefore(mfg)) {
            throw new IllegalArgumentException("ReceivedDate phải >= MfgDate");
        }
    }

    @Override
    public List<Solder> searchByCode(String keyword, int limit) {
        String kw = (keyword == null) ? "" : keyword.trim();
        if (kw.isEmpty()) return List.of();
        if (limit <= 0) limit = 20;

        // Đưa logic escape ký tự wildcard vào repo hoặc ở đây đều được
        return solderRepository.searchByCode(kw, limit);
    }

    @Override
    public List<String> suggestCodes(String keyword, int limit) {
        String kw = (keyword == null) ? "" : keyword.trim();
        if (kw.isEmpty()) return List.of();
        if (limit <= 0) limit = 10;

        return solderRepository.suggestCodes(kw, limit);
    }




}
