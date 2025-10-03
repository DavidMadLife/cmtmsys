package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.SolderSessionUpdate;
import org.chemtrovina.cmtmsys.model.Solder;

import java.time.LocalDate;
import java.util.List;

public interface SolderService {

    // CRUD
    void addSolder(Solder solder);
    void updateSolder(Solder solder);
    void deleteSolderById(int solderId);

    // Queries
    Solder getSolderById(int solderId);
    Solder getSolderByCode(String code);
    List<Solder> getAllSolders();

    // Helpers
    boolean existsByCode(String code);

    /**
     * Tìm kiếm linh hoạt (truyền null để bỏ tiêu chí).
     */
    List<Solder> search(String code,
                        String maker,
                        String lot,
                        LocalDate receivedFrom,
                        LocalDate receivedTo,
                        LocalDate expiryFrom,
                        LocalDate expiryTo);

    /** Danh sách hũ sắp/đã hết hạn trong khoảng [from, to] (theo ExpiryDate). */
    List<Solder> findExpiringBetween(LocalDate from, LocalDate to);


    List<Solder> searchByCode(String keyword, int limit);
    List<String> suggestCodes(String keyword, int limit);

}
