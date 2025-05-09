package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.MOQ;

import java.io.File;
import java.util.List;

public interface MOQService {
    List<MOQ> searchMOQ(String maker, String makerPN, String sapPN, String MOQ, String MSL);
    void saveImportedData(File file) ;
    void deleteById(int id);
    void updateImportedData(MOQ moq);
    void createMOQ(MOQ moq);
    MOQ getMOQbySAPPN(String sapPN);
    MOQ getMOQbyMakerPN(String makerPN);
}
