package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.base.MOQService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MOQServiceImpl implements MOQService {

    private final MOQRepository moqRepository;

    public MOQServiceImpl(MOQRepository moqRepository) {
        this.moqRepository = moqRepository;
    }

    @Override
    public List<MOQ> searchMOQ(String maker, String makerPN, String sapPN, String MOQ, String MSL) {
        return moqRepository.searchMOQ(maker, makerPN, sapPN, MOQ, MSL);
    }

    @Override
    public void saveImportedData(File file) {
        List<MOQ> moqs = moqRepository.importMoqFromExcel(file);

        List<String> existingSapPNs = moqRepository.getAllSapCodes();

        List<MOQ> toInsert = new ArrayList<>();
        List<MOQ> toUpdate = new ArrayList<>();

        for (MOQ moq : moqs) {
            if (existingSapPNs.contains(moq.getSapPN())) {
                // Tìm đúng ID để update
                MOQ existing = moqRepository.findBySapPN(moq.getSapPN());
                moq.setId(existing.getId());
                toUpdate.add(moq);
            } else {
                toInsert.add(moq);
            }
        }

        // Batch insert và batch update
        if (!toInsert.isEmpty()) {
            moqRepository.saveAll(toInsert);
        }
        if (!toUpdate.isEmpty()) {
            moqRepository.updateAll(toUpdate);
        }

        System.out.println("Tổng dòng đọc: " + moqs.size());
        System.out.println("-> Dòng thêm mới: " + toInsert.size());
        System.out.println("-> Dòng cập nhật: " + toUpdate.size());

    }

    @Override
    public void deleteById(int id) {
        moqRepository.delete(id);
    }

    @Override
    public void updateImportedData(MOQ moq) {
        moqRepository.update(moq);
    }

    @Override
    public List<MOQ> getAllMOQsByMakerPN(String makerPN) {
        return moqRepository.getAllMOQsByMakerPN(makerPN);
    }


    @Override
    public void createMOQ(MOQ moq) {
        moqRepository.add(moq);
    }

    @Override
    public MOQ getMOQbySAPPN(String sapPN) {
        return moqRepository.findBySapPN(sapPN);
    }

    @Override
    public MOQ getMOQbyMakerPN(String makerPN) {
        return moqRepository.findByMakerPN(makerPN);
    }

    @Override
    public List<String> getAllSapCodes() {
        return moqRepository.getAllSapCodes();
    }

    @Override
    public List<String> getAllMakers() {
        return moqRepository.getAllMakers();
    }
    @Override
    public List<String> getAllMakerPNs() {
        return moqRepository.getAllMakerPNs();
    }

    @Override
    public List<String> getAllMSLs() {
        return moqRepository.getAllMSLs();
    }

    @Override
    public void exportToExcel(List<MOQ> data, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("MOQ Data");

            // Header
            Row header = sheet.createRow(0);
            String[] columns = {"Maker", "MakerPN", "SAP P/N", "MOQ", "MSL"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            // Data rows
            for (int i = 0; i < data.size(); i++) {
                MOQ moq = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(moq.getMaker() != null ? moq.getMaker() : "");
                row.createCell(1).setCellValue(moq.getMakerPN() != null ? moq.getMakerPN() : "");
                row.createCell(2).setCellValue(moq.getSapPN() != null ? moq.getSapPN() : "");
                row.createCell(3).setCellValue(moq.getMoq());
                row.createCell(4).setCellValue(moq.getMsql() != null ? moq.getMsql() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }



}
