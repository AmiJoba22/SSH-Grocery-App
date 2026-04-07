package org.example;

import org.example.Data.Product;
import org.example.Data.User;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DataTest {

    @Test
    void testProduct() {
        Product product = new Product(1, "Apple", 0.99, 0.1, 1001, true);

        assertEquals(1, product.product_id);
        assertEquals("Apple", product.product_name);
        assertEquals(0.99, product.product_price);
        assertEquals(0.1, product.discount);
        assertEquals(1001, product.supplier_id);
        assertTrue(product.isAvailable);

        DecimalFormat df = new DecimalFormat("£0.00");
        assertEquals("Apple - £0.99 | Available", product.toString());
    }

    @Test
    void testUser() {
        User user = new User("9f87f607-a2b3-4f95-8e40-720f5a5f2a6c", "Emily Yang", "admin_3", 100002);

        assertEquals("9f87f607-a2b3-4f95-8e40-720f5a5f2a6c", user.getUser_id().toString());
        assertEquals("Emily Yang", user.getUser_name());
        assertEquals("admin_3", user.getShort_id());
        assertEquals(100002, user.getHouse_id());

        user.setUser_id("9f87f607-a2b3-4f95-8e40-720f5a5f2a6c");
        user.setUser_name("Emily Yang");
        user.setShort_id("admin_3");
        user.setHouse_id(100002);

        assertEquals("9f87f607-a2b3-4f95-8e40-720f5a5f2a6c", user.getUser_id().toString());
        assertEquals("Emily Yang", user.getUser_name());
        assertEquals("admin_3", user.getShort_id());
        assertEquals(100002, user.getHouse_id());
    }

    @Test
    void testItem() {
        Product product = new Product(1, "Apple", 0.99, 0.1, 1001, true);
        Data.Item item = new Data.Item(product, 5);

        assertEquals(product, item.getProduct());
        assertEquals(5, item.getAmount());
        assertEquals(4.95, item.getCost());
    }


}