package com.learnjava.completablefuture;

import static com.learnjava.util.CommonUtil.stopWatch;
import static com.learnjava.util.LoggerUtil.log;

import com.learnjava.domain.Inventory;
import com.learnjava.domain.Product;
import com.learnjava.domain.ProductInfo;
import com.learnjava.domain.ProductOption;
import com.learnjava.domain.Review;
import com.learnjava.service.InventoryService;
import com.learnjava.service.ProductInfoService;
import com.learnjava.service.ReviewService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProductServiceUsingCompletablaFuture {
    private ProductInfoService productInfoService;
    private ReviewService reviewService;
    private InventoryService inventoryervice;

    public ProductServiceUsingCompletablaFuture(ProductInfoService productInfoService, ReviewService reviewService) {
        this.productInfoService = productInfoService;
        this.reviewService = reviewService;
    }

    public ProductServiceUsingCompletablaFuture(ProductInfoService productInfoService, ReviewService reviewService, InventoryService inventoryervice) {
        this.productInfoService = productInfoService;
        this.reviewService = reviewService;
        this.inventoryervice = inventoryervice;
    }

    public Product retrieveProductDetails(String productId) {
        stopWatch.start();

        CompletableFuture<ProductInfo> cfProductInfo = CompletableFuture
                .supplyAsync(() -> productInfoService.retrieveProductInfo(productId));
        CompletableFuture<Review> cfReview = CompletableFuture
                .supplyAsync(() -> reviewService.retrieveReviews(productId));

        Product product = cfProductInfo
                .thenCombine(cfReview, (productInfo, review) -> new Product(productId, productInfo, review))
                .join(); //block the thread

        stopWatch.stop();
        log("Total Time Taken : " + stopWatch.getTime());
        return product;
    }


    public CompletableFuture<Product> retrieveProductDetails_approach2(String productId) {

        CompletableFuture<ProductInfo> cfProductInfo = CompletableFuture
                .supplyAsync(() -> productInfoService.retrieveProductInfo(productId));
        CompletableFuture<Review> cfReview = CompletableFuture
                .supplyAsync(() -> reviewService.retrieveReviews(productId));

        return cfProductInfo
                .thenCombine(cfReview, (productInfo, review) -> new Product(productId, productInfo, review));

    }


    public Product retrieveProductDetailsWithInventory(String productId) {
        stopWatch.start();

        CompletableFuture<ProductInfo> cfProductInfo = CompletableFuture
                .supplyAsync(() -> productInfoService.retrieveProductInfo(productId))
                .thenApply(productInfo -> {
                    productInfo.setProductOptions(updateInventory(productInfo));
                    return productInfo;
                });


        CompletableFuture<Review> cfReview = CompletableFuture
                .supplyAsync(() -> reviewService.retrieveReviews(productId));

        Product product = cfProductInfo
                .thenCombine(cfReview, (productInfo, review) -> new Product(productId, productInfo, review))
                .join(); //block the thread

        stopWatch.stop();
        log("Total Time Taken : " + stopWatch.getTime());
        return product;
    }

    public Product retrieveProductDetailsWithInventory_approach2(String productId) {
        stopWatch.start();

        CompletableFuture<ProductInfo> cfProductInfo = CompletableFuture
                .supplyAsync(() -> productInfoService.retrieveProductInfo(productId))
                .exceptionally((e) -> {
                    log("Handled the Exception in reviewService : " + e.getMessage());
                    return ProductInfo.builder()
                        .productId(productId)
                        .build();
                })
                .thenApply(productInfo -> {
                    productInfo.setProductOptions(updateInventory_approach2(productInfo));
                    return productInfo;
                });


        CompletableFuture<Review> cfReview = CompletableFuture
                .supplyAsync(() -> reviewService.retrieveReviews(productId))
                .exceptionally((e) -> {
                    log("Handled the Exception in reviewService : " + e.getMessage());
                    return Review.builder()
                            .noOfReviews(0).overallRating(0.0)
                            .build();
                });

        Product product = cfProductInfo
                .thenCombine(cfReview, (productInfo, review) -> new Product(productId, productInfo, review))
                .whenComplete((product1, throwable) -> {
                    log("Inside when Complete: " + product1 + "and Exception: " + throwable);
                })
                .join(); //block the thread

        stopWatch.stop();
        log("Total Time Taken : " + stopWatch.getTime());
        return product;
    }

    private List<ProductOption> updateInventory(ProductInfo productInfo) {

        List<ProductOption> productOptionList = productInfo.getProductOptions()
                .stream()
                .map(productOption -> {
                    Inventory inventory = inventoryervice.retrieveInventory(productOption);
                    productOption.setInventory(inventory);
                    return productOption;
                })
                .collect(Collectors.toList());
        return productOptionList;

    }

    private List<ProductOption> updateInventory_approach2(ProductInfo productInfo) {

        List<CompletableFuture<ProductOption>> productOptionList = productInfo.getProductOptions()

                .stream()
                .map(productOption -> {
                    return CompletableFuture.supplyAsync(() -> inventoryervice.retrieveInventory(productOption))
                            .thenApply(inventory -> {
                                productOption.setInventory(inventory);
                                return productOption;
                            });
                })
                .collect(Collectors.toList());

        return productOptionList.stream().map(CompletableFuture::join).collect(Collectors.toList());

    }

    private List<ProductOption> updateInventoryToProductOption_approach3Inventory_approach2(ProductInfo productInfo) {

        List<CompletableFuture<ProductOption>> productOptionList = productInfo.getProductOptions()
            .stream()
            .map(productOption -> {
                return CompletableFuture.supplyAsync(() -> inventoryervice.retrieveInventory(productOption))
                    .exceptionally((exception) ->{
                        log("Handled the Exception in InventoryService : " + exception.getMessage());
                        return Inventory.builder()
                            .count(1).build();
                    })
                    .thenApply(inventory -> {
                        productOption.setInventory(inventory);
                        return productOption;
                    });
            })
            .collect(Collectors.toList());

        CompletableFuture<Void> cfAllOf = CompletableFuture.allOf(productOptionList.toArray(new CompletableFuture[productOptionList.size()]));
        return cfAllOf.thenApply((v) -> {
            return productOptionList.stream().map(CompletableFuture::join).collect(Collectors.toList());
        })
        .join();



    }


    public static void main(String[] args) {

        ProductInfoService productInfoService = new ProductInfoService();
        ReviewService reviewService = new ReviewService();
        ProductServiceUsingCompletablaFuture productService = new ProductServiceUsingCompletablaFuture(productInfoService, reviewService);
        String productId = "ABC123";
        Product product = productService.retrieveProductDetails(productId);
        log("Product is " + product);

    }
}
