package com.example.bhojhon.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.bhojhon.model.FoodItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartManagerTest {

    private CartManager cart;

    private static FoodItem food(int id, double price) {
        return new FoodItem(id, "Item " + id, 1, price, "Main", "desc", null);
    }

    @BeforeEach
    void reset() {
        cart = CartManager.getInstance();
        cart.clearCart();
    }

    @Test
    void newCartIsEmpty() {
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItemCount());
        assertEquals(0.0, cart.getTotal());
    }

    @Test
    void addingItemsAccumulatesTotal() {
        cart.addItem(food(1, 250), 2); // 500
        cart.addItem(food(2, 80), 1);  // 80
        assertEquals(2, cart.getItemCount());
        assertEquals(580.0, cart.getTotal());
        assertFalse(cart.isEmpty());
    }

    @Test
    void addingSameItemMergesQuantity() {
        FoodItem biriyani = food(1, 250);
        cart.addItem(biriyani, 1);
        cart.addItem(biriyani, 2);
        assertEquals(1, cart.getItemCount(), "Same item should merge into one line");
        assertEquals(3, cart.getQuantity(biriyani));
        assertEquals(750.0, cart.getTotal());
    }

    @Test
    void nonPositiveQuantityIsIgnoredForNewItem() {
        cart.addItem(food(9, 100), 0);
        assertTrue(cart.isEmpty());
    }

    @Test
    void reducingQuantityToZeroRemovesLine() {
        FoodItem item = food(1, 100);
        cart.addItem(item, 2);
        cart.addItem(item, -2); // net zero -> removed
        assertTrue(cart.isEmpty());
    }

    @Test
    void formattedTotalUsesTakaSymbol() {
        cart.addItem(food(1, 199), 1);
        assertEquals("৳199", cart.getFormattedTotal());
    }
}
