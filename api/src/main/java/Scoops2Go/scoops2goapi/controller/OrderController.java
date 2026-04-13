package Scoops2Go.scoops2goapi.controller;

import Scoops2Go.scoops2goapi.dto.CheckoutDTO;
import Scoops2Go.scoops2goapi.dto.OrderDTO;
import Scoops2Go.scoops2goapi.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        OrderDTO dto = orderService.getOrderById(orderId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO incomingOrder) {
        OrderDTO outgoingOrder = orderService.createOrder(incomingOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(outgoingOrder);
    }

    @PutMapping
    public ResponseEntity<OrderDTO> updateOrder(@RequestBody OrderDTO incomingOrder) {
        OrderDTO outgoingOrder = orderService.updateOrder(incomingOrder);
        return ResponseEntity.ok(outgoingOrder);
    }

    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<CheckoutDTO> checkoutOrder(@PathVariable Long orderId) {
        CheckoutDTO checkoutDTO = orderService.checkoutOrder(orderId);
        return ResponseEntity.ok(checkoutDTO);
    }

    // Delete / clear an order (empty the basket and remove the order)
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
