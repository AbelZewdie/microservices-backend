package com.programmingtechie.order_service.controller;

import com.programmingtechie.order_service.dto.OrderRequest;
import com.programmingtechie.order_service.dto.OrderResponse;
import com.programmingtechie.order_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final Executor taskExecutor; // <-- Inject Spring's managed thread pool

    @PostMapping
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<ResponseEntity<String>> placeOrder(@RequestBody OrderRequest orderRequest) {
        return CompletableFuture.supplyAsync(() ->
                ResponseEntity.status(HttpStatus.CREATED)
                        .body(orderService.placeOrder(orderRequest)), taskExecutor);
    }

    public CompletableFuture<ResponseEntity<String>> fallbackMethod(OrderRequest orderRequest, Throwable throwable) {
        return CompletableFuture.supplyAsync(() ->
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Oops! Something went wrong, please order after some time!"), taskExecutor);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
}