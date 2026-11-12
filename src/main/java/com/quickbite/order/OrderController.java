package com.quickbite.order;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class OrderController {

    private final OrderRepository orders;
    private final MenuClient menu;

    public OrderController(OrderRepository orders, MenuClient menu) {
        this.orders = orders;
        this.menu = menu;
    }

    // COMMAND - calls menu-service to validate items and get authoritative prices
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder createOrder(@RequestBody CreateOrderRequest req) {
        if (req.customerId() == null || req.restaurantId() == null
                || req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "customerId, restaurantId and items are required");
        }
        CustomerOrder order = new CustomerOrder();
        order.customerId = req.customerId();
        order.restaurantId = req.restaurantId();
        double total = 0;
        for (CreateOrderRequest.Line line : req.items()) {
            int qty = line.qty() == null ? 1 : line.qty();
            if (line.itemId() == null || qty < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemId and qty >= 1 required");
            }
            ItemDto item;
            try {
                item = menu.getItem(line.itemId());
            } catch (RestClientException e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "menu-service unavailable, try again later");
            }
            if (item == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "item " + line.itemId() + " not found");
            }
            if (!item.restaurantId().equals(req.restaurantId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "item " + item.id() + " does not belong to this restaurant");
            }
            if (!item.available()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "item " + item.name() + " is unavailable");
            }
            OrderLine ol = new OrderLine();
            ol.itemId = item.id();
            ol.name = item.name();
            ol.qty = qty;
            ol.unitPrice = item.price();
            order.items.add(ol);
            total += item.price() * qty;
        }
        order.total = Math.round(total * 100.0) / 100.0;
        return orders.save(order);
    }

    // QUERY
    @GetMapping("/orders/{id}")
    public CustomerOrder getOrder(@PathVariable Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }

    // QUERY
    @GetMapping("/orders")
    public List<CustomerOrder> listOrders(@RequestParam(required = false) Long customerId) {
        return customerId == null ? orders.findAll() : orders.findByCustomerId(customerId);
    }

    // COMMAND
    @PutMapping("/orders/{id}/cancel")
    public CustomerOrder cancel(@PathVariable Long id) {
        CustomerOrder o = getOrder(id);
        if (!"CONFIRMED".equals(o.status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot cancel order in " + o.status + " state");
        }
        o.status = "CANCELLED";
        return orders.save(o);
    }
}
