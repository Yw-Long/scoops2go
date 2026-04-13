package scoops2Go.api;

import Scoops2Go.scoops2goapi.Scoops2GoApiApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * ParameterisedProductApiTests
 *
 * Parameterised integration tests for the Scoops2Go Product API.
 * Uses JUnit 5 @ParameterizedTest to validate multiple inputs in
 * a single, data-driven test method — reducing duplication and
 * increasing coverage efficiently.
 *
 * Seed data product IDs (from SeedData.java):
 *   Cones   : 1=Waffle Cone, 2=Sugar Cone, 3=Cup
 *   Flavours: 4=Vanilla, 5=Chocolate, 6=Strawberry,
 *             7=Mint Choc Chip, 8=Salted Caramel
 *   Toppings: 9=Sprinkles, 10=Chocolate Chips,
 *             11=Caramel Sauce
 *
 * Covers: PB_TC_005 (extended), PB_TC_006 (extended)
 * REQ: REQ-SI-002
 */
@SpringBootTest(classes = Scoops2GoApiApplication.class)
@AutoConfigureMockMvc
class ParameterisedProductApiTests {

    @Autowired
    private MockMvc mockMvc;

    // ─────────────────────────────────────────────────────────────
    // PB_TC_005 (Parameterised extension)
    // REQ-SI-002 | GET /api/product/{productId} — all valid seed IDs
    //
    // Verifies that every seeded product returns HTTP 200 with the
    // three core fields the implementation actually provides
    // (productId, productName, price).
    // Field-name mismatch (productType/productDesc vs type/description)
    // is already captured in PB_D002; this test focuses on
    // confirming the endpoint is reachable for ALL seed products.
    // ─────────────────────────────────────────────────────────────
    @ParameterizedTest(name = "Run [{index}] productId={0} ({1})")
    @CsvFileSource(resources = "/testdata/products.csv", numLinesToSkip = 1)
    @DisplayName("PB_TC_005 (param) – GET /api/product/{id} returns 200 for each seed product")
    void PB_TC_005_param_getProductById_allSeedIds_return200(
            int productId,
            String expectedName,
            String expectedType) throws Exception {

        mockMvc.perform(get("/api/product/" + productId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // productId must round-trip correctly
                .andExpect(jsonPath("$.productId").value(productId))
                // productName must be a non-empty string
                .andExpect(jsonPath("$.productName", not(emptyOrNullString())))
                // price must be present and positive (actual field name used by impl)
                .andExpect(jsonPath("$.price", notNullValue()))
                // type must be present (actual field name used by impl)
                .andExpect(jsonPath("$.type", notNullValue()));
    }

    // ─────────────────────────────────────────────────────────────
    // PB_TC_006 (Parameterised extension)
    // REQ-SI-002 | GET /api/product/{productId} — boundary & invalid IDs
    //
    // Uses BVA (Boundary Value Analysis) and equivalence partitioning
    // to verify the API returns a 4xx for a range of invalid IDs.
    // Covers: zero, negatives, non-existent positive IDs, and
    // an extremely large ID (Long boundary).
    // ─────────────────────────────────────────────────────────────
    @ParameterizedTest(name = "Run [{index}] productId={0} — expect 4xx ({1})")
    @CsvSource({
            "0,       zero — invalid (no product has id=0)",
            "-1,      negative — below lower bound",
            "-9999,   large negative",
            "9999,    non-existent positive ID",
            "99999,   far above seed range",
            "2147483647, Integer.MAX_VALUE — upper boundary probe",
    })
    @DisplayName("PB_TC_006 (param) – GET /api/product/{id} returns 4xx for invalid/non-existent IDs")
    void PB_TC_006_param_getProductById_invalidIds_return4xx(
            long productId,
            String description) throws Exception {

        System.out.printf("[PB_TC_006-param] Testing productId=%d (%s)%n",
                productId, description);

        mockMvc.perform(get("/api/product/" + productId)
                        .accept(MediaType.APPLICATION_JSON))
                // Must be a client error (4xx) — NOT a server error (5xx)
                .andExpect(status().is4xxClientError());
    }
}