package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.*;
import org.chemtrovina.cmtmsys.model.Feeder;

public class FeederActionRow {
    private final IntegerProperty feederId = new SimpleIntegerProperty();
    private final StringProperty feederCode = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public FeederActionRow(int feederId, String feederCode, String status) {
        this.feederId.set(feederId);
        this.feederCode.set(feederCode);
        this.status.set(status);
    }

    public static FeederActionRow fromFeeder(Feeder feeder, String status) {
        return new FeederActionRow(feeder.getFeederId(), feeder.getFeederCode(), status);
    }

    public int getFeederId() {
        return feederId.get();
    }

    public IntegerProperty feederIdProperty() {
        return feederId;
    }

    public String getFeederCode() {
        return feederCode.get();
    }

    public StringProperty feederCodeProperty() {
        return feederCode;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}
