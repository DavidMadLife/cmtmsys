package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.MaterialCart;

import java.util.List;

public interface MaterialCartService {
    MaterialCart createCart(String cartCode);

    List<MaterialCart> getAllCarts();
    MaterialCart getCartById(int cartId);
    MaterialCart getCartByCode(String cartCode);
}
