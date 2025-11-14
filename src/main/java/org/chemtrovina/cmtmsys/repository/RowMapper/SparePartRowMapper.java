package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.SparePart;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class SparePartRowMapper implements RowMapper<SparePart> {

    @Override
    public SparePart mapRow(ResultSet rs, int rowNum) throws SQLException {
        SparePart sparePart = new SparePart();

        sparePart.setId(rs.getInt("Id"));
        sparePart.setDate(rs.getObject("Date", LocalDate.class));
        sparePart.setName(rs.getString("Name"));
        sparePart.setCode(rs.getString("Code"));
        sparePart.setImageData(rs.getBytes("ImageData"));
        sparePart.setSupplier(rs.getString("Supplier"));
        sparePart.setManufacturer(rs.getString("Manufacturer"));
        sparePart.setQuantity(rs.getInt("Quantity"));
        sparePart.setUnit(rs.getString("Unit"));
        sparePart.setWarehouseKeeper(rs.getString("WarehouseKeeper"));
        sparePart.setNote(rs.getString("Note"));

        return sparePart;
    }
}
