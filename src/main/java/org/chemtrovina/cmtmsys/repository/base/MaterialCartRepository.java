package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.MaterialCart;

import java.util.List;

public interface MaterialCartRepository {
    void insert(MaterialCart cart);
    List<MaterialCart> findAll();
    MaterialCart findById(int cartId);
    MaterialCart findByCode(String cartCode);
}
