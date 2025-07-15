package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class ModelLineRun {
    private int runId;
    private int modelLineId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String status;
    private String runCode;

    public ModelLineRun() {
        this.startedAt = LocalDateTime.now();
        this.status = "Running";
    }

    // Getters & Setters
    public int getRunId() { return runId; }
    public void setRunId(int runId) { this.runId = runId; }

    public int getModelLineId() { return modelLineId; }
    public void setModelLineId(int modelLineId) { this.modelLineId = modelLineId; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public String getRunCode() {
        return runCode;
    }

    public void setRunCode(String runCode) {
        this.runCode = runCode;
    }

}
