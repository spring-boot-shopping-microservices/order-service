package org.example.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.InventoryResponse;
import org.example.orderservice.dto.ItemRequest;
import org.example.orderservice.dto.OrderRequest;
import org.example.orderservice.model.Item;
import org.example.orderservice.model.Order;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {
  private final OrderRepository orderRepository;

  private final WebClient webClient;

  @Autowired
  public OrderService(OrderRepository orderRepository, WebClient webClient) {
    this.orderRepository = orderRepository;
    this.webClient = webClient;
  }

  public String placeOrder(OrderRequest orderRequest) {
    Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .items(orderRequest.getItemRequests()
                    .stream()
                    .map(this::mapToItems)
                    .toList())
            .build();

    List<String> skuCodeList = order.getItems().stream()
            .map(Item::getSkuCode)
            .toList();

    // Call inventory-service to check if item is in stock
    InventoryResponse[] inventoryResponseList = webClient
            .get()
            .uri("http://localhost:8082/api/inventory",
                    uriBuilder -> uriBuilder.queryParam("skuCodeList", skuCodeList)
                            .build())
            .retrieve()
            .bodyToMono(InventoryResponse[].class)
            .block();

    boolean allItemsInStock = Arrays.stream(Objects.requireNonNull(inventoryResponseList))
            .allMatch(InventoryResponse::isInStock);

    if (Boolean.TRUE.equals(allItemsInStock)) {
      orderRepository.save(order);

      log.info("order created : {}", order);

      return "Order placed successfully. Order ID: " + order.getOrderNumber();
    } else {
      throw new IllegalStateException("Product is not in stock");
    }
  }

  private String generateOrderNumber() {
    String orderNumber = "ORD-" + UUID.randomUUID();
    log.info("order number created : {}", orderNumber);
    return orderNumber;
  }

  private Item mapToItems(ItemRequest itemRequests) {
    return Item.builder()
            .skuCode(itemRequests.getSkuCode())
            .price(itemRequests.getPrice())
            .quantity(itemRequests.getQuantity())
            .build();
  }
}
