package com.quickbite.order;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceTests {

    @Autowired
    MockMvc mvc;

    @MockBean
    MenuClient menuClient;   // menu-service is mocked: this service is tested in isolation

    @Test
    void createAndCancelOrder() throws Exception {
        when(menuClient.getItem(1L)).thenReturn(new ItemDto(1L, 1L, "Biryani", 200.0, true));
        when(menuClient.getItem(9L)).thenReturn(null);

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":1,\"restaurantId\":1,\"items\":[{\"itemId\":1,\"qty\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(400.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mvc.perform(put("/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(put("/orders/1/cancel")).andExpect(status().isConflict());

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":1,\"restaurantId\":1,\"items\":[{\"itemId\":9}]}"))
                .andExpect(status().isNotFound());
    }
}
