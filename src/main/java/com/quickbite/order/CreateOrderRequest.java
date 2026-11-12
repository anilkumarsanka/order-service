package com.quickbite.order;

import java.util.List;

public record CreateOrderRequest(Long customerId, Long restaurantId, List<Line> items) {
    public record Line(Long itemId, Integer qty) {
    }
}
