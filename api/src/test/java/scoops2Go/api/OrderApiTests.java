package scoops2Go.api;

import Scoops2Go.scoops2goapi.Scoops2GoApiApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Order API Integration Tests
 * Covers: BK_TC_010, BK_TC_014A/B, BK_TC_015, BK_TC_017, OT_TC_004, OT_TC_005
 */
@SpringBootTest(classes = Scoops2GoApiApplication.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Shared order ID captured in setup, reused across tests
    private long sharedOrderId = -1;

    // Valid treat: Waffle Cone (id=1) + Vanilla (id=4)
    private static final String VALID_ORDER_PAYLOAD = """
            {
              "basketItems": [
                {
                  "products": [
                    { "productId": 1 },
                    { "productId": 4 }
                  ]
                }
              ],
              "promotion": null
            }
            """;

    // Setup: create a shared order and capture its orderId
    @BeforeAll
    void setUp_createSharedTestOrder() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER_PAYLOAD)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String body   = result.getResponse().getContentAsString();
        int    status = result.getResponse().getStatus();

        System.out.println("[Setup] POST /api/order → HTTP " + status);
        System.out.println("[Setup] Response: " + body);

        if (status == 200 || status == 201) {
            JsonNode json = objectMapper.readTree(body);
            if (json.has("orderId")) {
                sharedOrderId = json.get("orderId").asLong();
            }
        }
        System.out.println("[Setup] sharedOrderId = " + sharedOrderId);
    }

    // BK_TC_010 – POST valid order returns 2xx with orderId, orderTotal, deliveryCost=2.50, non-empty basketItems
    @Test
    @Order(1)
    @DisplayName("BK_TC_010 – POST /api/order valid treat returns 2xx with required fields")
    void BK_TC_010_postOrder_validPayload_returns2xx() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER_PAYLOAD)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId",    greaterThan(0)))
                .andExpect(jsonPath("$.orderTotal", greaterThan(0.0)))
                .andExpect(jsonPath("$.deliveryCost").value(2.50))
                .andExpect(jsonPath("$.basketItems", not(empty())));
    }

    // BK_TC_014A – POST order with empty products list returns 4xx
    @Test
    @Order(2)
    @DisplayName("BK_TC_014A – POST /api/order empty products returns 4xx")
    void BK_TC_014A_postOrder_emptyProducts_returns4xx() throws Exception {
        String payload = """
                {
                  "basketItems": [
                    { "products": [] }
                  ],
                  "promotion": null
                }
                """;

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    // BK_TC_014B – POST order with 4 flavours (exceeds max of 3) returns 4xx
    @Test
    @Order(3)
    @DisplayName("BK_TC_014B – POST /api/order 4 Flavours (max is 3) returns 4xx")
    void BK_TC_014B_postOrder_fourFlavours_returns4xx() throws Exception {
        String payload = """
                {
                  "basketItems": [
                    {
                      "products": [
                        { "productId": 1 },
                        { "productId": 4 },
                        { "productId": 5 },
                        { "productId": 6 },
                        { "productId": 7 }
                      ]
                    }
                  ],
                  "promotion": null
                }
                """;

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    // OT_TC_004 – GET existing order by ID returns 200 with all required fields
    @Test
    @Order(4)
    @DisplayName("OT_TC_004 – GET /api/order/{id} valid order returns 200 with all fields")
    void OT_TC_004_getOrder_validId_returns200() throws Exception {
        Assumptions.assumeTrue(sharedOrderId > 0,
                "Skipped: setup order creation failed – see BK_TC_010 result");

        mockMvc.perform(get("/api/order/" + sharedOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(sharedOrderId))
                .andExpect(jsonPath("$.orderTotal",  greaterThan(0.0)))
                .andExpect(jsonPath("$.deliveryCost", notNullValue()))
                .andExpect(jsonPath("$.basketItems",  not(empty())));
    }

    // OT_TC_005 – GET non-existent order ID returns 404
    @Test
    @Order(5)
    @DisplayName("OT_TC_005 – GET /api/order/99999 returns 404 Not Found")
    void OT_TC_005_getOrder_invalidId_returns404() throws Exception {
        mockMvc.perform(get("/api/order/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // BK_TC_015 – PUT order updates basket and recalculates total, orderId unchanged
    @Test
    @Order(6)
    @DisplayName("BK_TC_015 – PUT /api/order updates basket and returns 200")
    void BK_TC_015_putOrder_validUpdate_returns200() throws Exception {
        Assumptions.assumeTrue(sharedOrderId > 0,
                "Skipped: setup order creation failed – see BK_TC_010 result");

        String payload = String.format("""
                {
                  "orderId": %d,
                  "basketItems": [
                    {
                      "products": [
                        { "productId": 2 },
                        { "productId": 5 }
                      ]
                    }
                  ],
                  "promotion": null
                }
                """, sharedOrderId);

        mockMvc.perform(put("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(sharedOrderId))
                .andExpect(jsonPath("$.basketItems", not(empty())))
                .andExpect(jsonPath("$.orderTotal",  greaterThan(0.0)));
    }

    // BK_TC_017 – POST checkout returns 200 with paid=true, transactionId, and matching orderId
    @Test
    @Order(7)
    @DisplayName("BK_TC_017 – POST /api/order/{id}/checkout returns 200 with CheckoutDTO")
    void BK_TC_017_postCheckout_validOrder_returnsCheckoutDTO() throws Exception {
        Assumptions.assumeTrue(sharedOrderId > 0,
                "Skipped: setup order creation failed – see BK_TC_010 result");

        String checkoutUrl = "/api/order/" + sharedOrderId + "/checkout";
        String paymentPayload = """
                {
                  "cardNumber": "4242424242424242"
                }
                """;

        MvcResult probe = mockMvc.perform(post(checkoutUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentPayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        System.out.println("[BK_TC_017] POST " + checkoutUrl
                + " → HTTP " + probe.getResponse().getStatus());
        System.out.println("[BK_TC_017] Response: "
                + probe.getResponse().getContentAsString());

        mockMvc.perform(post(checkoutUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentPayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.transactionId", not(emptyOrNullString())))
                .andExpect(jsonPath("$.order",          notNullValue()))
                .andExpect(jsonPath("$.order.orderId").value(sharedOrderId));
    }
}