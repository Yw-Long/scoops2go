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
 * Product API parameterized integration tests.
 * Data-driven tests for reduced duplication and better coverage.
 *
 * Seed product IDs:
 * Cones: 1,2,3 | Flavours: 4-8 | Toppings:9-11
 *
 * Covers: PB_TC_005, PB_TC_006
 */
@SpringBootTest(classes = Scoops2GoApiApplication.class)
@AutoConfigureMockMvc
class ParameterisedProductApiTests {

    @Autowired
    private MockMvc mockMvc;

    // PB_TC_005: Test all valid seed product IDs
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
                // Validate productId
                .andExpect(jsonPath("$.productId").value(productId))
                // Validate productName
                .andExpect(jsonPath("$.productName", not(emptyOrNullString())))
                // Validate price exists
                .andExpect(jsonPath("$.price", notNullValue()))
                // Validate type exists
                .andExpect(jsonPath("$.type", notNullValue()));
    }

    // PB_TC_006: Test invalid and boundary IDs
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
                // Expect 4xx client error
                .andExpect(status().is4xxClientError());
    }
}