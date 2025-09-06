package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class MaterialCart {
    private int cartId;
    private String cartCode;
    private LocalDateTime createdAt;

    public MaterialCart() {

    }
    public MaterialCart(int cartId, String cartCode, LocalDateTime createdAt) {
        this.cartId = cartId;
        this.cartCode = cartCode;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public String getCartCode() {
        return cartCode;
    }

    public void setCartCode(String cartCode) {
        this.cartCode = cartCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return cartCode; // hoặc format thêm createdAt nếu muốn
    }

}
