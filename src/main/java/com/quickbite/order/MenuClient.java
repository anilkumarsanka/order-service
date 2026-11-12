package com.quickbite.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** Synchronous REST client for menu-service (3 second timeouts). */
@Component
public class MenuClient {

    private final RestClient client;

    public MenuClient(@Value("${menu.service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Returns the item, or null if menu-service answers 404. Other failures throw RestClientException. */
    public ItemDto getItem(Long id) {
        try {
            return client.get().uri("/items/{id}", id).retrieve().body(ItemDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}
