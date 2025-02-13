package com.learnjava.completablefuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.learnjava.domain.Product;
import com.learnjava.service.InventoryService;
import com.learnjava.service.ProductInfoService;
import com.learnjava.service.ReviewService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProductServiceUsingCompletablaFutureTest {

    private ProductInfoService pis = new ProductInfoService();
    private ReviewService rs = new ReviewService();
    private InventoryService is = new InventoryService();
    ProductServiceUsingCompletablaFuture pscf = new
            ProductServiceUsingCompletablaFuture(pis,rs, is);

    @Test
    void retrieveProductDetails() {
        //given
        String productId="ABC123";

        //when
        Product product = pscf.retrieveProductDetails(productId);

        //then
        assertNotNull(product);
        assertTrue(product.getProductInfo().getProductOptions().size()>0);
        assertNotNull(product.getReview());
    }

    @Test
    void retrieveProductDetailsWithInventory() {
        //given
        String productId="ABC123";

        //when
        Product product = pscf.retrieveProductDetailsWithInventory(productId);

        //then
        assertNotNull(product);
        assertTrue(product.getProductInfo().getProductOptions().size()>0);
        product.getProductInfo().getProductOptions()
                .forEach(productOption -> {
                    assertNotNull(productOption.getInventory());
                });
        assertNotNull(product.getReview());
    }

    @Test
    void retrieveProductDetailsWithInventory_approach2() {
        //given
        String productId="ABC123";

        //when
        Product product = pscf.retrieveProductDetailsWithInventory_approach2(productId);

        //then
        assertNotNull(product);
        assertTrue(product.getProductInfo().getProductOptions().size()>0);
        product.getProductInfo().getProductOptions()
                .forEach(productOption -> {
                    assertNotNull(productOption.getInventory());
                });
        assertNotNull(product.getReview());
    }

    @Test
    void retrieveProductDetailsWithInventory_productInfoServiceError() {
        //given
        String productId="ABC123";
        when(pis.retrieveProductInfo(any())).thenThrow(new RuntimeException("Exception Occurred"));
        when(rs.retrieveReviews(any())).thenThrow(new RuntimeException("Exception Occurred"));
//        when(is.retrieveInventory(any())).thenCallRealMethod();

        //when
//        Product product = pscf.retrieveProductDetailsWithInventory_approach2(productId);

        //then
        Assertions.assertThrows(RuntimeException.class, () -> {
            pscf.retrieveProductDetailsWithInventory_approach2(productId);
        });
    }
}