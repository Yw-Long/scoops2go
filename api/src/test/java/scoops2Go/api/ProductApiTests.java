package scoops2Go.api;

import Scoops2Go.scoops2goapi.Scoops2GoApiApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Product API integration tests.
 * Covers: PB_TC_004, PB_TC_005, PB_TC_006
 */
@SpringBootTest(classes = Scoops2GoApiApplication.class)
@AutoConfigureMockMvc
class ProductApiTests {

    @Autowired
    private MockMvc mockMvc;

    // PB_TC_004: Test singular endpoint
    @Test
    @DisplayName("PB_TC_004 – GET /api/product (singular) returns 200 with product array")
    void PB_TC_004_getProductList_singularEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/product")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].productId", notNullValue()));
    }

    // PB_TC_004: Test plural endpoint (per requirement)
    @Test
    @DisplayName("PB_TC_004 – GET /api/products (plural – per REQ-SI-001) returns 200 with product array")
    void PB_TC_004_getProductList_pluralEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", not(empty())));
    }

    // PB_TC_005: Test correct field names (expected to fail)
    @Test
    @DisplayName("PB_TC_005 – GET /api/product/1 returns 200 with REQ-specified field names")
    void PB_TC_005_getProductById_validId_returns200WithCorrectFieldNames() throws Exception {
        mockMvc.perform(get("/api/product/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName", not(emptyOrNullString())))
                .andExpect(jsonPath("$.productType", notNullValue()))
                .andExpect(jsonPath("$.productDesc", not(emptyOrNullString())))
                .andExpect(jsonPath("$.productPrice", notNullValue()));
    }

    // PB_TC_005: Test actual field names
    @Test
    @DisplayName("PB_TC_005 – GET /api/product/1 confirms ACTUAL field names returned by implementation")
    void PB_TC_005_getProductById_validId_confirmsActualFieldNames() throws Exception {
        mockMvc.perform(get("/api/product/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.type", notNullValue()))
                .andExpect(jsonPath("$.description", not(emptyOrNullString())))
                .andExpect(jsonPath("$.price", notNullValue()));
    }

    // PB_TC_006: Test non-existent ID returns 4xx
    @Test
    @DisplayName("PB_TC_006 – GET /api/product/99999 returns 4xx for non-existent product")
    void PB_TC_006_getProductById_nonExistentId_returns4xx() throws Exception {
        mockMvc.perform(get("/api/product/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    // PB_TC_006: Test non-existent ID returns 404
    @Test
    @DisplayName("PB_TC_006 – GET /api/product/99999 returns specifically 404 Not Found")
    void PB_TC_006_getProductById_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/product/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}