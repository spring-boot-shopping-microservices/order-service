package org.example.orderservice.controller;

import org.example.orderservice.dto.OrderRequest;
import org.example.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public String placeOrder(@RequestBody OrderRequest orderRequest) {
    return orderService.placeOrder(orderRequest);
  }
}
