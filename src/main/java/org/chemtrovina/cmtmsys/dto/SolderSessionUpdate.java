// org.chemtrovina.cmtmsys.dto.SolderSessionUpdate.java
package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDateTime;

public class SolderSessionUpdate {
    private Integer warehouseId;
    private Integer receiverEmployeeId;
    private LocalDateTime openTime;
    private Integer returnEmployeeId;
    private LocalDateTime returnTime;
    private LocalDateTime scrapTime;
    private String returnStatus;
    private String note;

    // builder gọn tay
    public SolderSessionUpdate withWarehouseId(Integer v){ this.warehouseId=v; return this; }
    public SolderSessionUpdate withReceiverEmployeeId(Integer v){ this.receiverEmployeeId=v; return this; }
    public SolderSessionUpdate withOpenTime(LocalDateTime v){ this.openTime=v; return this; }
    public SolderSessionUpdate withReturnEmployeeId(Integer v){ this.returnEmployeeId=v; return this; }
    public SolderSessionUpdate withReturnTime(LocalDateTime v){ this.returnTime=v; return this; }
    public SolderSessionUpdate withScrapTime(LocalDateTime v){ this.scrapTime=v; return this; }
    public SolderSessionUpdate withReturnStatus(String v){ this.returnStatus=v; return this; }
    public SolderSessionUpdate withNote(String v){ this.note=v; return this; }

    public Integer getWarehouseId(){ return warehouseId; }
    public Integer getReceiverEmployeeId(){ return receiverEmployeeId; }
    public LocalDateTime getOpenTime(){ return openTime; }
    public Integer getReturnEmployeeId(){ return returnEmployeeId; }
    public LocalDateTime getReturnTime(){ return returnTime; }
    public LocalDateTime getScrapTime(){ return scrapTime; }
    public String getReturnStatus(){ return returnStatus; }
    public String getNote(){ return note; }
}
