package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Solder;

import java.time.LocalDate;
import java.util.List;

public interface SolderRepository {

    // CRUD
    void add(Solder solder);
    void update(Solder solder);
    void deleteById(int solderId);

    // Queries
    Solder findById(int solderId);
    Solder findByCode(String code);
    List<Solder> findAll();

    // Helpers
    boolean existsByCode(String code);

    /**
     * Tìm kiếm linh hoạt. Truyền null để bỏ tiêu chí.
     * - code  : tìm một mã cụ thể hoặc null
     * - maker : lọc maker (LIKE %maker%)
     * - lot   : lọc lot (LIKE %lot%)
     * - receivedFrom/To : khoảng ngày nhập
     * - expiryFrom/To   : khoảng hạn sử dụng
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
