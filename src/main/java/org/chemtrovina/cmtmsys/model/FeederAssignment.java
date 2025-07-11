package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class FeederAssignment {
    private int assignmentId;
    private int runId;
    private int feederId;
    private LocalDateTime assignedAt;
    private String assignedBy;

    public FeederAssignment() {
        this.assignedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getRunId() { return runId; }
    public void setRunId(int runId) { this.runId = runId; }

    public int getFeederId() { return feederId; }
    public void setFeederId(int feederId) { this.feederId = feederId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
}
