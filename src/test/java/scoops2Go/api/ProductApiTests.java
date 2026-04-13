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
 * ProductApiTests
 *
 * Automated integration tests for the Scoops2Go Product API endpoints.
 * Covers test cases: PB_TC_004, PB_TC_005, PB_TC_006
 *
 * Prerequisites:
 *   - Spring Boot application context loads with default seed data.
 *   - H2 in-memory database is used for testing.
 *
 * Run with: mvn test  OR  right-click in IntelliJ → Run 'ProductApiTests'
 */
@SpringBootTest(classes = Scoops2GoApiApplication.class)
@AutoConfigureMockMvc
class ProductApiTests {

    @Autowired
    private MockMvc mockMvc;

    // ─────────────────────────────────────────────────────────────
    // PB_TC_004
    // REQ-SI-001 | GET /api/product
    // Verify that the product list endpoint returns 200 with a valid
    // JSON array containing all required fields on each product.
    //
    // NOTE: Requirements spec specifies the URL as /api/products (plural).
    //       The actual implementation uses /api/product (singular).
    //       Both variants are tested below. One is expected to FAIL —
    //       record this as a defect (field-name / endpoint mismatch).
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PB_TC_004 – GET /api/product (singular) returns 200 with product array")
    void PB_TC_004_getProductList_singularEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/product")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Response body must be a JSON array
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                // Array must not be empty
                .andExpect(jsonPath("$", not(empty())))
                // Every item must carry a productId
                .andExpect(jsonPath("$[0].productId", notNullValue()));
    }

    @Test
    @DisplayName("PB_TC_004 – GET /api/products (plural – per REQ-SI-001) returns 200 with product array")
    void PB_TC_004_getProductList_pluralEndpoint_returns200() throws Exception {
        // Per REQ-SI-001 the URL should be /api/products (plural).
        // This test is expected to FAIL if the implementation uses /api/product (singular).
        // Record the result in the Defect Log if status is not 200.
        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", not(empty())));
    }

    // ─────────────────────────────────────────────────────────────
    // PB_TC_005
    // REQ-SI-002 | GET /api/product/{productId} – valid ID (1)
    // Verify the endpoint returns HTTP 200 and a product object with
    // the field names defined in the Requirements Specification.
    //
    // KNOWN ISSUE (pre-recorded FAIL): The implementation returns
    //   "type"        instead of  "productType"
    //   "description" instead of  "productDesc"
    //   "price"       instead of  "productPrice"
    // The assertions below use the REQ-specified names so this test
    // will FAIL, confirming the defect.
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PB_TC_005 – GET /api/product/1 returns 200 with REQ-specified field names")
    void PB_TC_005_getProductById_validId_returns200WithCorrectFieldNames() throws Exception {
        mockMvc.perform(get("/api/product/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // productId must equal 1
                .andExpect(jsonPath("$.productId").value(1))
                // productName must be a non-empty string
                .andExpect(jsonPath("$.productName", not(emptyOrNullString())))
                // REQ-SI-002 specifies these exact field names ↓
                // These assertions are EXPECTED TO FAIL (actual code returns "type", "description", "price")
                .andExpect(jsonPath("$.productType", notNullValue()))
                .andExpect(jsonPath("$.productDesc", not(emptyOrNullString())))
                .andExpect(jsonPath("$.productPrice", notNullValue()));
    }

    @Test
    @DisplayName("PB_TC_005 – GET /api/product/1 confirms ACTUAL field names returned by implementation")
    void PB_TC_005_getProductById_validId_confirmsActualFieldNames() throws Exception {
        // This complementary test documents what the API ACTUALLY returns.
        // Use the results to populate the "Actual Result" column in the test case spec.
        mockMvc.perform(get("/api/product/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                // Actual field names (differ from Requirements) – logged here for evidence
                .andExpect(jsonPath("$.type", notNullValue()))
                .andExpect(jsonPath("$.description", not(emptyOrNullString())))
                .andExpect(jsonPath("$.price", notNullValue()));
    }

    // ─────────────────────────────────────────────────────────────
    // PB_TC_006
    // REQ-SI-002 | GET /api/product/{productId} – invalid ID (99999)
    // Verify the endpoint returns a 4xx client error (expected: 404)
    // and does NOT return a 500 Internal Server Error.
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PB_TC_006 – GET /api/product/99999 returns 4xx for non-existent product")
    void PB_TC_006_getProductById_nonExistentId_returns4xx() throws Exception {
        mockMvc.perform(get("/api/product/99999")
                        .accept(MediaType.APPLICATION_JSON))
                // Must be a client-error (4xx), NOT a server error (5xx)
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PB_TC_006 – GET /api/product/99999 returns specifically 404 Not Found")
    void PB_TC_006_getProductById_nonExistentId_returns404() throws Exception {
        // REST convention expects 404. Record actual status in Defect Log if different.
        mockMvc.perform(get("/api/product/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}