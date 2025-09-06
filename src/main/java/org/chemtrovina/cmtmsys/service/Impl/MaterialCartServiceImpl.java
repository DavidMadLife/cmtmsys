package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.MaterialCart;
import org.chemtrovina.cmtmsys.repository.base.MaterialCartRepository;
import org.chemtrovina.cmtmsys.service.base.MaterialCartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialCartServiceImpl implements MaterialCartService {

    private final MaterialCartRepository cartRepo;

    public MaterialCartServiceImpl(MaterialCartRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    @Override
    public MaterialCart createCart(String cartCode) {
        if (cartCode == null || cartCode.isBlank()) {
            cartCode = "CART-" + System.currentTimeMillis(); // hoặc UUID.randomUUID().toString()
        }

        MaterialCart cart = new MaterialCart();
        cart.setCartCode(cartCode);
        cart.setCreatedAt(java.time.LocalDateTime.now());

        cartRepo.insert(cart);

        return cartRepo.findByCode(cartCode); // hoặc cart nếu ID được auto-set khi insert
    }


    @Override
    public List<MaterialCart> getAllCarts() {
        return cartRepo.findAll();
    }

    @Override
    public MaterialCart getCartById(int cartId) {
        return cartRepo.findById(cartId);
    }

    @Override
    public MaterialCart getCartByCode(String cartCode) {
        return cartRepo.findByCode(cartCode);
    }
}
