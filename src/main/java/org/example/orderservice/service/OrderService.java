package org.example.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.ItemRequest;
import org.example.orderservice.dto.OrderRequest;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.Item;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class OrderService {
  private final OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public String placeOrder(OrderRequest orderRequest) {
    Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .items(orderRequest.getItemRequests()
                    .stream()
                    .map(this::mapToItems)
                    .toList())
            .build();

    orderRepository.save(order);

    log.info("order created : {}", order);

    return "Order placed successfully. Order ID: " + order.getOrderNumber();
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
