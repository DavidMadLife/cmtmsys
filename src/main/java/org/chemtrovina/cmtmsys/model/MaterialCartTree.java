package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class MaterialCartTree {
    private int treeId;
    private int cartId;
    private String treeCode;
    private LocalDateTime createdAt;
    private String levelNote;

    public String getLevelNote() {
        return levelNote;
    }

    public void setLevelNote(String levelNote) {
        this.levelNote = levelNote;
    }

    // Getter & Setter
    public int getTreeId() {
        return treeId;
    }

    public void setTreeId(int treeId) {
        this.treeId = treeId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public String getTreeCode() {
        return treeCode;
    }

    public void setTreeCode(String treeCode) {
        this.treeCode = treeCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
