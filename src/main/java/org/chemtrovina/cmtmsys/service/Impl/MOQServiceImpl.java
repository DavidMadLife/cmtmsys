package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.base.MOQService;

import java.io.File;
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

        // Lấy hết MakerPN đang có trong database
        List<String> existingMakerPNs = moqRepository.findAllMakerPNs();

        List<MOQ> toInsert = new ArrayList<>();
        List<MOQ> toUpdate = new ArrayList<>();

        for (MOQ moq : moqs) {
            if (existingMakerPNs.contains(moq.getMakerPN())) {
                // Tìm đúng ID để update
                MOQ existing = moqRepository.findByMakerPN(moq.getMakerPN());
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


}
