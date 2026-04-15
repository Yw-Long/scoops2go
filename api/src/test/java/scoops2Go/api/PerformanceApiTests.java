package scoops2Go.api;

import Scoops2Go.scoops2goapi.Scoops2GoApiApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API performance tests.
 * Covers: PERF_TC_003
 */
@SpringBootTest(classes = Scoops2GoApiApplication.class)
@AutoConfigureMockMvc
class PerformanceApiTests {

    @Autowired
    private MockMvc mockMvc;

    // Response time threshold
    private static final long RESPONSE_TIME_THRESHOLD_MS = 500L;

    // Test run count
    private static final int REPEAT_COUNT = 3;

    // PERF_TC_003: Test response time ≤ 500ms
    @Test
    @DisplayName("PERF_TC_003 – GET /api/product responds within 500 ms (REQ-PER-002)")
    void PERF_TC_003_getProductList_respondsWithin500ms() throws Exception {

        for (int run = 1; run <= REPEAT_COUNT; run++) {

            long start = System.currentTimeMillis();

            MvcResult result = mockMvc.perform(get("/api/product")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            long elapsed = System.currentTimeMillis() - start;

            System.out.printf("[PERF_TC_003] Run %d/%d → HTTP %d  elapsed: %d ms%n",
                    run, REPEAT_COUNT,
                    result.getResponse().getStatus(),
                    elapsed);

            // Verify response time
            assertTrue(elapsed <= RESPONSE_TIME_THRESHOLD_MS,
                    String.format(
                            "Run %d exceeded threshold: %d ms > %d ms (REQ-PER-002)",
                            run, elapsed, RESPONSE_TIME_THRESHOLD_MS));
        }
    }
}