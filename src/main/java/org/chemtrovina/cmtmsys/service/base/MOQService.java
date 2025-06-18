package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.MOQ;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface MOQService {
    List<MOQ> searchMOQ(String maker, String makerPN, String sapPN, String MOQ, String MSL);
    void saveImportedData(File file) ;
    void deleteById(int id);
    void updateImportedData(MOQ moq);
    void createMOQ(MOQ moq);
    MOQ getMOQbySAPPN(String sapPN);
    MOQ getMOQbyMakerPN(String makerPN);
    List<String> getAllSapCodes();
    List<String> getAllMakerPNs();
    List<String> getAllMakers();
    List<String> getAllMSLs();
    //List<String> getAllInvoicePNs();
    void exportToExcel(List<MOQ> data, File file) throws IOException;
    List<MOQ> getAllMOQsByMakerPN(String makerPN);

}
