package com.multistorecommerce.store;

import com.multistorecommerce.configuration.*;
import com.multistorecommerce.store.api.StoreController;
import com.multistorecommerce.store.application.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StoreController.class, properties = "app.cors.allowed-origins=http://localhost:4200")
@Import({SecurityConfiguration.class, ApiExceptionHandler.class})
class StoreApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean StoreService service;
    @Test void publicListingReturnsExplicitDto() throws Exception {
        var id = UUID.randomUUID();
        when(service.listActive()).thenReturn(List.of(new StoreSummary(id, "centro", "Centro")));
        mvc.perform(get("/api/v1/stores")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id.toString()))
            .andExpect(jsonPath("$[0].slug").value("centro"))
            .andExpect(jsonPath("$[0].name").value("Centro"))
            .andExpect(jsonPath("$[0].active").doesNotExist());
    }
    @Test void emptyListingIsAnArray() throws Exception {
        when(service.listActive()).thenReturn(List.of());
        mvc.perform(get("/api/v1/stores")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void unmatchedAndWriteRoutesAreDenied() throws Exception {
        for (String path : List.of("/api/v1/orders", "/actuator/env", "/v3/api-docs", "/login")) {
            mvc.perform(get(path)).andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }
        mvc.perform(post("/api/v1/stores")).andExpect(status().isForbidden());
    }
    @Test void onlyExplicitOriginsAreAllowed() throws Exception {
        mvc.perform(options("/api/v1/stores").header("Origin", "http://localhost:4200")
            .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
            .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
        mvc.perform(options("/api/v1/stores").header("Origin", "https://untrusted.example")
            .header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
    }
    @Test void internalErrorsDoNotLeakDetails() throws Exception {
        when(service.listActive()).thenThrow(new IllegalStateException("secret database password"));
        mvc.perform(get("/api/v1/stores")).andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.detail").value("The request could not be completed. Please try again later."))
            .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
}
