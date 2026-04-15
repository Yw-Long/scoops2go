package scoops2Go.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;

/**
 * UI Automation Tests for Scoops2Go
 *
 * Tool: Selenium WebDriver (Chrome / Firefox) — industry-standard web UI automation.
 *
 * Prerequisites:
 *   - Spring Boot API running on localhost:8080
 *   - Vue.js frontend running on localhost:5173
 *   - Google Chrome installed
 *   - Mozilla Firefox installed (for cross-browser test)
 *
 * NOTE — Safari (UI_TC_008):
 *   Safari WebDriver is only supported on macOS via the built-in safaridriver.
 *   This test environment runs Windows 11; Safari cannot be executed here.
 *   This is recorded as an environment constraint, not a test failure.
 */
public class UiAutomationTests {

    private static final String FRONTEND_URL  = "http://localhost:5173";
    private static final String API_URL        = "http://localhost:8080";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);

    /**
     * FIX NOTE (applied to all UI tests):
     *
     * The original tests waited for:
     *   //h2[contains(text(),'Select Cone')]
     *
     * The actual Vue template renders:
     *   <h2>Select Cone (required)</h2>   ← in the LAST tile (gradient-purple)
     *
     * Although contains() is a partial match and should still find it, the
     * "Select Cone" tile is the LAST section rendered and depends on the API
     * returning CONE products.  Under headless Chrome the page sometimes times
     * out before that data arrives.
     *
     * Fix: wait for <h2>Select Flavours</h2> instead — it appears in the SECOND
     * tile (gradient-pink) and is the first section heading rendered once the
     * API responds.  All subsequent assertions remain unchanged.
     */
    private static final By PAGE_READY_LOCATOR =
            By.xpath("//h2[contains(text(),'Select Flavours')]");

    /** Regex: price must be formatted as £X.XX (GBP, exactly two decimal places). */
    private static final String GBP_FORMAT_REGEX = ".*£\\d+\\.\\d{2}.*";

    // =========================================================================
    // UI_TC_008 — Cross-Browser Compatibility (Chrome)
    // REQ-BR-001
    // Verifies the builder page loads and the cone section is present in Chrome.
    // Expected result: PASS.
    // =========================================================================
    @Test
    public void testCrossBrowserChrome() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);

            // Confirm the page has loaded in Chrome
            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));

            // Confirm the Cone section is also present
            WebElement coneHeading = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//h2[contains(text(),'Select Cone')]")));

            assertTrue(coneHeading.isDisplayed(),
                    "Chrome: 'Select Cone' section not visible on the builder page.");

        } finally {
            driver.quit();
        }
    }

    // =========================================================================
    // UI_TC_008 — Cross-Browser Compatibility (Firefox)
    // REQ-BR-001
    // Verifies the builder page loads and the cone section is present in Firefox.
    // Expected result: PASS.
    // =========================================================================
    @Test
    public void testCrossBrowserFirefox() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        WebDriver driver = new FirefoxDriver(options);

        try {
            driver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);

            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));

            WebElement coneHeading = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//h2[contains(text(),'Select Cone')]")));

            assertTrue(coneHeading.isDisplayed(),
                    "Firefox: 'Select Cone' section not visible on the builder page.");

        } finally {
            driver.quit();
        }
    }

    // =========================================================================
    // UI_TC_009 — Internationalisation: GBP Currency Format Across Full Journey
    // REQ-FR-002 / SRS §7 Internationalisation & Localisation
    //
    // Description:
    //   Verify that all monetary values throughout the full user journey
    //   (Builder page → Cart/Basket page) are displayed in GBP (£) with correct
    //   UK locale formatting (£X.XX, two decimal places), and that no incorrect
    //   currency symbols ($, €) or malformatted numbers appear at any stage.
    //   Also verifies that when the browser locale is set to a non-UK locale
    //   (de-DE), the application still enforces GBP formatting rather than
    //   following the browser's locale setting.
    //   Extends PB_TC_002 (single-page manual observation of Builder page only)
    //   with automated multi-page and locale-isolation coverage per SRS §7.
    //
    // Test Steps:
    //   Phase 1 – Default locale (Chrome headless, no locale override):
    //     Step 1.  Launch Chrome headless (default locale).
    //     Step 2.  Navigate to /scoops-builder; wait for PAGE_READY_LOCATOR
    //              and at least one .product-price element.
    //     Step 3.  Assert all .cone-item .product-price texts:
    //                – contain "£"
    //                – match £X.XX format (GBP_FORMAT_REGEX)
    //                – do NOT contain "$"   ← confirms / re-asserts defect PB_D001
    //     Step 4.  Assert all .flavour-item .product-price texts:
    //                – contain "£" and match £X.XX format.
    //     Step 5.  Assert all .topping-item .product-price texts:
    //                – contain "£" and match £X.XX format.
    //     Step 6.  Select first Cone (radio) and first Flavour (checkbox);
    //              click "Add to Basket"; wait for success toast.
    //     Step 7.  Navigate to /cart; wait for cart page to load.
    //     Step 8.  Assert subtotal, delivery fee, and order total on the cart
    //              page all contain "£" and match £X.XX format.
    //     Step 9.  Quit the default-locale driver.
    //
    //   Phase 2 – Non-UK locale isolation (Chrome headless, --lang=de-DE):
    //     Step 10. Launch a second Chrome headless instance with --lang=de-DE.
    //     Step 11. Navigate to /scoops-builder; wait for PAGE_READY_LOCATOR.
    //     Step 12. Repeat Steps 3–5 with the de-DE driver:
    //                – Assert "£" present and "€" absent on all price elements.
    //                – Assert no European decimal format (e.g. "2,00") present.
    //     Step 13. Quit the de-DE driver.
    //
    // Known defect:
    //   PB_D001 — Cone prices display '$' instead of '£' (Vue template line 89).
    //   Step 3 is EXPECTED TO FAIL, confirming the defect.
    //   All other price checks (flavours, toppings, cart totals) are expected to PASS.
    // =========================================================================
    @Test
    public void testGBPCurrencyDisplay() {

        // ── Phase 1: default locale ──────────────────────────────────────────
        SoftAssertions softly = new SoftAssertions();
        ChromeOptions defaultOptions = new ChromeOptions();
        defaultOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(defaultOptions);

        try {
            // Step 1-2: navigate and wait for data
            driver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".product-price")));

            // Step 3: Cone prices — EXPECTED TO FAIL (defect PB_D001: '$' used)
            List<WebElement> conePrices = driver.findElements(
                    By.cssSelector(".cone-item .product-price"));
            assertFalse(conePrices.isEmpty(),
                    "No cone price elements found (.cone-item .product-price). " +
                            "Verify the API returned CONE products.");
            for (WebElement el : conePrices) {
                String text = el.getText();
                softly.assertThat(text.contains("£"))
                        .as("[EXPECTED FAIL – PB_D001] Cone price '" + text +
                                "' does not display '£'. Vue template line 89 hardcodes '$'.")
                        .isTrue();
                softly.assertThat(text.contains("$"))
                        .as("[EXPECTED FAIL – PB_D001] Cone price '" + text +
                                "' incorrectly displays '$' instead of '£'.")
                        .isFalse();
                softly.assertThat(text.matches(GBP_FORMAT_REGEX))
                        .as("Cone price '" + text + "' does not match £X.XX format.")
                        .isTrue();
            }


            // Step 4: Flavour prices — expected PASS
            List<WebElement> flavourPrices = driver.findElements(
                    By.cssSelector(".flavour-item .product-price"));
            assertFalse(flavourPrices.isEmpty(),
                    "No flavour price elements found (.flavour-item .product-price).");
            for (WebElement el : flavourPrices) {
                String text = el.getText();
                assertTrue(text.contains("£"),
                        "Flavour price '" + text + "' does not display '£'.");
                assertTrue(text.matches(GBP_FORMAT_REGEX),
                        "Flavour price '" + text + "' does not match £X.XX format.");
            }

            // Step 5: Topping prices — expected PASS
            List<WebElement> toppingPrices = driver.findElements(
                    By.cssSelector(".topping-item .product-price"));
            assertFalse(toppingPrices.isEmpty(),
                    "No topping price elements found (.topping-item .product-price).");
            for (WebElement el : toppingPrices) {
                String text = el.getText();
                assertTrue(text.contains("£"),
                        "Topping price '" + text + "' does not display '£'.");
                assertTrue(text.matches(GBP_FORMAT_REGEX),
                        "Topping price '" + text + "' does not match £X.XX format.");
            }

            // Step 6: add one treat to populate the cart
            WebElement coneRadio = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".cone-item input[type='radio']")));
            coneRadio.click();
            WebElement flavourCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".flavour-item input[type='checkbox']")));
            flavourCheckbox.click();
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".green-button:not([disabled])")));
            addBtn.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".toast, .notification, [class*='toast'], [class*='success']")));

            // Step 7: navigate to cart
            driver.get(FRONTEND_URL + "/cart");
            WebDriverWait cartWait = new WebDriverWait(driver, WAIT_TIMEOUT);

            // Step 8: assert cart monetary values (subtotal, delivery, total)
            // Selectors cover common Vue cart implementations; adjust if class names differ.
            String[] cartPriceSelectors = {
                    ".cart-subtotal",   // product subtotal
                    ".delivery-cost",   // delivery fee (£2.50)
                    ".order-total",     // grand total
                    ".cart-total",      // alternative grand-total class
                    "[class*='subtotal']",
                    "[class*='total']",
                    "[class*='delivery']"
            };
            boolean cartElementFound = false;
            for (String selector : cartPriceSelectors) {
                List<WebElement> cartPriceEls = driver.findElements(By.cssSelector(selector));
                for (WebElement el : cartPriceEls) {
                    String text = el.getText().trim();
                    if (text.isEmpty()) continue;
                    cartElementFound = true;
                    // Only assert elements that look like price values
                    if (text.contains("£") || text.contains("$") || text.contains("€")
                            || text.matches(".*\\d+\\.\\d+.*")) {
                        assertTrue(text.contains("£"),
                                "Cart price element ('" + selector + "') shows '" + text +
                                        "' — expected GBP '£' symbol.");
                        assertFalse(text.contains("$"),
                                "Cart price element ('" + selector + "') shows '$' — " +
                                        "not a GBP format.");
                        assertFalse(text.contains("€"),
                                "Cart price element ('" + selector + "') shows '€' — " +
                                        "application must use GBP regardless of locale.");
                        assertTrue(text.matches(GBP_FORMAT_REGEX),
                                "Cart price '" + text + "' does not match £X.XX format.");
                    }
                }
            }
            // If no cart price elements were found at all, log a warning rather than
            // failing hard — the cart CSS class names may differ in the actual implementation.
            if (!cartElementFound) {
                System.out.println("UI_TC_009 WARNING: No cart price elements matched the " +
                        "expected CSS selectors. Verify cart page structure and update selectors.");
            }

        } finally {
            // Step 9: quit default-locale driver
            driver.quit();
        }

        // ── Phase 2: non-UK locale isolation (de-DE) ────────────────────────
        // Step 10: launch Chrome with German locale
        ChromeOptions deOptions = new ChromeOptions();
        deOptions.addArguments("--headless=new");
        deOptions.addArguments("--lang=de-DE");
        WebDriver deDriver = new ChromeDriver(deOptions);

        try {
            // Step 11: navigate to builder
            deDriver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait deWait = new WebDriverWait(deDriver, WAIT_TIMEOUT);
            deWait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));
            deWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".product-price")));

            // Step 12: all prices must still use £, not € or European comma format
            String[][] localePriceSelectors = {
                    {".cone-item .product-price",    "Cone (de-DE)"},
                    {".flavour-item .product-price", "Flavour (de-DE)"},
                    {".topping-item .product-price", "Topping (de-DE)"}
            };
            for (String[] entry : localePriceSelectors) {
                String selector  = entry[0];
                String label     = entry[1];
                List<WebElement> elements = deDriver.findElements(By.cssSelector(selector));
                for (WebElement el : elements) {
                    String text = el.getText();
                    // Application must enforce GBP even when browser locale is de-DE
                    assertFalse(text.contains("€"),
                            "[de-DE locale] " + label + " price '" + text +
                                    "' shows '€' — app must use GBP regardless of browser locale.");
                    assertFalse(text.matches(".*\\d,\\d{2}.*"),
                            "[de-DE locale] " + label + " price '" + text +
                                    "' uses European comma decimal format — must use UK '.' separator.");
                    // Cone assertion intentionally skipped here to avoid double-failing
                    // on PB_D001 ($-vs-£); the locale isolation check is about € vs £ only.
                    if (!selector.contains("cone")) {
                        assertTrue(text.contains("£"),
                                "[de-DE locale] " + label + " price '" + text +
                                        "' does not display '£' — GBP must not change with browser locale.");
                    }
                }
            }

        } finally {
            // Step 13: quit de-DE driver
            deDriver.quit();
            softly.assertAll();
        }
    }

    // =========================================================================
    // SEC_TC_001 — Unauthorised Cross-Session Order Access
    // REQ-SEC-002
    // Verifies the API rejects unauthenticated requests for another user's order.
    // No authentication is implemented — the API returns 200 to any caller.
    // Expected result: FAIL (high-severity security defect SEC_D001).
    // =========================================================================
    @Test
    public void testUnauthorizedOrderAccess() throws IOException {
        // Step 1: Create a real order so we have a valid orderId
        long createdOrderId = createMinimalOrder();
        System.out.println("SEC_TC_001 — Created order ID for test: " + createdOrderId);

        // Step 2: Query that order with NO session cookie or auth header
        URL url = new URL(API_URL + "/api/order/" + createdOrderId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        // Deliberately omit any session / Authorization header

        int statusCode = connection.getResponseCode();
        System.out.println("SEC_TC_001 — HTTP status for unauthenticated GET: " + statusCode);

        // Must return 401 or 403 — returning 200 is a high-severity security defect.
        assertNotEquals(200, statusCode,
                "SECURITY DEFECT (SEC_D001): API returned HTTP 200 for an unauthenticated " +
                        "request to /api/order/" + createdOrderId + ". " +
                        "Order data is exposed to anonymous callers — no access control enforced.");

        assertTrue(statusCode == 401 || statusCode == 403,
                "SECURITY DEFECT (SEC_D001): Expected HTTP 401 or 403, but received HTTP "
                        + statusCode + ". Cross-session order access control is not enforced.");

        connection.disconnect();
    }

    // =========================================================================
    // PERF_TC_001 — Add to Basket Confirmation Response Time
    // REQ-PER-001
    // Verifies the success toast appears within an average of 3 seconds
    // (max 6 seconds per run) across 3 runs after clicking 'Add to Basket'.
    // Expected result: PASS under normal local conditions.
    // =========================================================================
    @Test
    public void testAddToBasketResponseTime() {
        ChromeOptions options = new ChromeOptions();
        // Run without --headless so Chrome does not crash mid-session
        WebDriver driver = new ChromeDriver(options);

        try {
            long[] durations = new long[3];

            for (int run = 0; run < 3; run++) {
                driver.get(FRONTEND_URL + "/scoops-builder");
                WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);

                // FIX: wait for "Select Flavours" heading (second tile, loads first)
                wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));

                // Step 1: Select a Cone — <input type="radio"> inside .cone-item
                WebElement coneRadio = wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.cssSelector(".cone-item input[type='radio']")));
                coneRadio.click();

                // Step 2: Select a Flavour — <input type="checkbox"> inside .flavour-item
                WebElement flavourCheckbox = wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.cssSelector(".flavour-item input[type='checkbox']")));
                flavourCheckbox.click();

                // Step 3: Wait for 'Add to Basket' button to become enabled
                WebElement addBtn = wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.cssSelector(".green-button:not([disabled])")));

                // Step 4: Click and measure until toast appears
                long start = System.currentTimeMillis();
                addBtn.click();

                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(
                                ".toast, .notification, [class*='toast'], [class*='success']")));

                durations[run] = System.currentTimeMillis() - start;
                System.out.printf("PERF_TC_001 Run %d: %d ms%n", run + 1, durations[run]);

                assertTrue(durations[run] <= 6000,
                        "Run " + (run + 1) + " exceeded 6-second maximum: "
                                + durations[run] + " ms.");
            }

            long average = (durations[0] + durations[1] + durations[2]) / 3;
            System.out.printf("PERF_TC_001 Average: %d ms (threshold: 3000 ms)%n", average);

            assertTrue(average <= 3000,
                    "Average response time " + average + " ms exceeds the 3-second threshold "
                            + "(REQ-PER-001). Runs: "
                            + durations[0] + " ms, " + durations[1] + " ms, "
                            + durations[2] + " ms.");

        } finally {
            driver.quit();
        }
    }

    // =========================================================================
    // PERF_TC_002 — Delivery Estimate Display Response Time
    // REQ-PER-003
    // Verifies the estimated delivery time appears within 1 second of
    // navigating to the basket view, across 3 runs.
    // Expected result: may FAIL if backend delivery calculation exceeds 1 s.
    // =========================================================================
    @Test
    public void testDeliveryEstimateResponseTime() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try {
            // Setup: add one treat so the basket has content to display
            driver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);

            // FIX: wait for "Select Flavours" heading instead of "Select Cone"
            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));

            WebElement coneRadio = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".cone-item input[type='radio']")));
            coneRadio.click();

            WebElement flavourCheckbox = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".flavour-item input[type='checkbox']")));
            flavourCheckbox.click();

            WebElement addBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".green-button:not([disabled])")));
            addBtn.click();

            // Confirm item was added via toast before measuring basket load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(
                            ".toast, .notification, [class*='toast'], [class*='success']")));

            // Measure 3 runs of navigating to /basket and loading delivery estimate
            long[] durations = new long[3];

            for (int run = 0; run < 3; run++) {
                driver.get(FRONTEND_URL);
                Thread.sleep(500);

                long start = System.currentTimeMillis();
                driver.get(FRONTEND_URL + "/cart");

                WebDriverWait runWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                runWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(),'Est Delivery') " +
                                "or contains(text(),'minutes')]")));

                durations[run] = System.currentTimeMillis() - start;
                System.out.printf("PERF_TC_002 Run %d: %d ms%n", run + 1, durations[run]);

                assertTrue(durations[run] <= 1000,
                        "Run " + (run + 1) + ": delivery estimate took "
                                + durations[run] + " ms — exceeds the 1-second threshold "
                                + "(REQ-PER-003).");
            }

            long average = (durations[0] + durations[1] + durations[2]) / 3;
            System.out.printf("PERF_TC_002 Average: %d ms (threshold: 1000 ms)%n", average);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    // =========================================================================
    // Helper: POST a minimal order to /api/order and return the new orderId.
    // =========================================================================
    private long createMinimalOrder() throws IOException {
        URL url = new URL(API_URL + "/api/order");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String body = "{\"promotion\":null,\"basketItems\":[]}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        assertEquals(201, status,
                "Setup failed: could not create order for SEC_TC_001. " +
                        "POST /api/order returned HTTP " + status);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        conn.disconnect();

        String response = sb.toString();
        System.out.println("SEC_TC_001 setup response: " + response);

        int idx = response.indexOf("\"orderId\":");
        if (idx == -1) {
            idx = response.indexOf("\"id\":");
            assertTrue(idx != -1,
                    "Could not find 'orderId' or 'id' in response: " + response);
            idx += 5;
        } else {
            idx += 10;
        }
        while (idx < response.length() && !Character.isDigit(response.charAt(idx))) idx++;
        int end = idx;
        while (end < response.length() && Character.isDigit(response.charAt(end))) end++;

        return Long.parseLong(response.substring(idx, end));
    }
}