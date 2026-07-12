package com.shopwise.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ShopWiseApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void protectedEndpointWithoutTokenReturnsNormalized401() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").exists());

        mvc.perform(get("/api/products").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void merchantCannotCreateProduct() throws Exception {
        String token = login("merchant", "merchant123");
        mvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Interdit\",\"price\":10}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void saleAndRecommendationWorkEndToEnd() throws Exception {
        String token = login("merchant", "merchant123");
        String saleBody = "{\"items\":[{\"productId\":1,\"quantity\":2},{\"productId\":2,\"quantity\":1}]}";

        String saleJson = mvc.perform(post("/api/sales")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(saleBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(2899.97))
                .andExpect(jsonPath("$.lines.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        Integer saleId = JsonPath.read(saleJson, "$.id");

        mvc.perform(get("/api/sales/{id}", saleId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy").value("merchant"));

        mvc.perform(get("/api/recommendations").param("productId", "1").param("limit", "2")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(2))
                .andExpect(jsonPath("$[0].score").value(1.0));
    }

    @Test
    void validationAndNotFoundErrorsUseStandardShape() throws Exception {
        String token = login("merchant", "merchant123");
        mvc.perform(post("/api/sales")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mvc.perform(get("/api/sales/999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private String login(String username, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}
