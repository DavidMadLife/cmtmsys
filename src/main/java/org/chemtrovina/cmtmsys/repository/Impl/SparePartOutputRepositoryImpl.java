package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.SparePartOutput;
import org.chemtrovina.cmtmsys.repository.base.SparePartOutputRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SparePartOutputRepositoryImpl implements SparePartOutputRepository {

    private final JdbcTemplate jdbcTemplate;

    public SparePartOutputRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(SparePartOutput output) {
        String sql = """
            INSERT INTO SparePartOutputs 
                (SparePartId, Quantity, Issuer, Receiver, Line, Note, OutputDate)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                output.getSparePartId(),
                output.getQuantity(),
                output.getIssuer(),
                output.getReceiver(),
                output.getLine(),
                output.getNote(),
                output.getOutputDate()
        );
    }

    @Override
    public List<SparePartOutput> findAll() {
        String sql = """
            SELECT 
                o.Id, 
                o.SparePartId, 
                o.Quantity, 
                o.Issuer, 
                o.Receiver, 
                o.Line, 
                o.Note, 
                o.OutputDate,

                s.Name AS SparePartName,
                s.Code AS SparePartCode,
                s.ImageData,
                s.Supplier AS Model,
                s.Manufacturer AS Serial
            FROM SparePartOutputs o
            JOIN SpareParts s ON o.SparePartId = s.Id
            ORDER BY o.OutputDate DESC
        """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public List<SparePartOutput> findByDateRange(String from, String to) {
        String sql = """
            SELECT 
                o.Id, 
                o.SparePartId, 
                o.Quantity, 
                o.Issuer, 
                o.Receiver, 
                o.Line, 
                o.Note, 
                o.OutputDate,

                s.Name AS SparePartName,
                s.Code AS SparePartCode,
                s.ImageData,
                s.Supplier AS Model,
                s.Manufacturer AS Serial
            FROM SparePartOutputs o
            JOIN SpareParts s ON o.SparePartId = s.Id
            WHERE o.OutputDate BETWEEN ? AND ?
            ORDER BY o.OutputDate DESC
        """;

        return jdbcTemplate.query(sql, this::mapRow, from, to);
    }

    private SparePartOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
        SparePartOutput sp = new SparePartOutput();

        sp.setId(rs.getInt("Id"));
        sp.setSparePartId(rs.getInt("SparePartId"));
        sp.setQuantity(rs.getInt("Quantity"));
        sp.setIssuer(rs.getString("Issuer"));
        sp.setReceiver(rs.getString("Receiver"));
        sp.setLine(rs.getString("Line"));
        sp.setNote(rs.getString("Note"));

        var ts = rs.getTimestamp("OutputDate");
        sp.setOutputDate(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());

        // Dữ liệu JOIN từ SpareParts
        sp.setSparePartName(rs.getString("SparePartName"));
        sp.setSparePartCode(rs.getString("SparePartCode"));
        sp.setModel(rs.getString("Model"));       // lấy từ Supplier
        sp.setSerial(rs.getString("Serial"));     // lấy từ Manufacturer
        sp.setImageData(rs.getBytes("ImageData"));

        return sp;
    }
}
