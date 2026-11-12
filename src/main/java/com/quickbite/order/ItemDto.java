package com.quickbite.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Shape of the item returned by menu-service GET /items/{id}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemDto(Long id, Long restaurantId, String name, double price, boolean available) {
}
