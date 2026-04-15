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
 * Selenium UI tests for Scoops2Go
 */
public class UiAutomationTests {

    private static final String FRONTEND_URL  = "http://localhost:5173";
    private static final String API_URL        = "http://localhost:8080";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final By PAGE_READY_LOCATOR = By.xpath("//h2[contains(text(),'Select Flavours')]");
    private static final String GBP_FORMAT_REGEX = ".*£\\d+\\.\\d{2}.*";

    // UI_TC_008: Chrome compatibility test
    @Test
    public void testCrossBrowserChrome() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));

            WebElement coneHeading = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//h2[contains(text(),'Select Cone')]")));

            assertTrue(coneHeading.isDisplayed());

        } finally {
            driver.quit();
        }
    }

    // UI_TC_008: Firefox compatibility test
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

            assertTrue(coneHeading.isDisplayed());

        } finally {
            driver.quit();
        }
    }

    // UI_TC_009: GBP currency format validation
    @Test
    public void testGBPCurrencyDisplay() {
        SoftAssertions softly = new SoftAssertions();
        ChromeOptions defaultOptions = new ChromeOptions();
        defaultOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(defaultOptions);

        try {
            driver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-price")));

            List<WebElement> conePrices = driver.findElements(By.cssSelector(".cone-item .product-price"));
            assertFalse(conePrices.isEmpty());
            for (WebElement el : conePrices) {
                String text = el.getText();
                softly.assertThat(text.contains("£")).isTrue();
                softly.assertThat(text.contains("$")).isFalse();
                softly.assertThat(text.matches(GBP_FORMAT_REGEX)).isTrue();
            }

            List<WebElement> flavourPrices = driver.findElements(By.cssSelector(".flavour-item .product-price"));
            assertFalse(flavourPrices.isEmpty());
            for (WebElement el : flavourPrices) {
                String text = el.getText();
                assertTrue(text.contains("£"));
                assertTrue(text.matches(GBP_FORMAT_REGEX));
            }

            List<WebElement> toppingPrices = driver.findElements(By.cssSelector(".topping-item .product-price"));
            assertFalse(toppingPrices.isEmpty());
            for (WebElement el : toppingPrices) {
                String text = el.getText();
                assertTrue(text.contains("£"));
                assertTrue(text.matches(GBP_FORMAT_REGEX));
            }

            WebElement coneRadio = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".cone-item input[type='radio']")));
            coneRadio.click();
            WebElement flavourCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".flavour-item input[type='checkbox']")));
            flavourCheckbox.click();
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".green-button:not([disabled])")));
            addBtn.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".toast, .notification, [class*='toast'], [class*='success']")));

            driver.get(FRONTEND_URL + "/cart");
            String[] cartPriceSelectors = {".cart-subtotal", ".delivery-cost", ".order-total", ".cart-total", "[class*='subtotal']", "[class*='total']", "[class*='delivery']"};
            boolean cartElementFound = false;
            for (String selector : cartPriceSelectors) {
                List<WebElement> cartPriceEls = driver.findElements(By.cssSelector(selector));
                for (WebElement el : cartPriceEls) {
                    String text = el.getText().trim();
                    if (text.isEmpty()) continue;
                    cartElementFound = true;
                    if (text.contains("£") || text.contains("$") || text.contains("€") || text.matches(".*\\d+\\.\\d+.*")) {
                        assertTrue(text.contains("£"));
                        assertFalse(text.contains("$"));
                        assertFalse(text.contains("€"));
                        assertTrue(text.matches(GBP_FORMAT_REGEX));
                    }
                }
            }
            if (!cartElementFound) {
                System.out.println("UI_TC_009 WARNING: No cart price elements found");
            }

        } finally {
            driver.quit();
        }

        ChromeOptions deOptions = new ChromeOptions();
        deOptions.addArguments("--headless=new");
        deOptions.addArguments("--lang=de-DE");
        WebDriver deDriver = new ChromeDriver(deOptions);

        try {
            deDriver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait deWait = new WebDriverWait(deDriver, WAIT_TIMEOUT);
            deWait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));
            deWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-price")));

            String[][] localePriceSelectors = {
                    {".cone-item .product-price", "Cone (de-DE)"},
                    {".flavour-item .product-price", "Flavour (de-DE)"},
                    {".topping-item .product-price", "Topping (de-DE)"}
            };
            for (String[] entry : localePriceSelectors) {
                String selector = entry[0];
                String label = entry[1];
                List<WebElement> elements = deDriver.findElements(By.cssSelector(selector));
                for (WebElement el : elements) {
                    String text = el.getText();
                    assertFalse(text.contains("€"));
                    assertFalse(text.matches(".*\\d,\\d{2}.*"));
                    if (!selector.contains("cone")) {
                        assertTrue(text.contains("£"));
                    }
                }
            }

        } finally {
            deDriver.quit();
            softly.assertAll();
        }
    }

    // SEC_TC_001: Unauthorized order access test
    @Test
    public void testUnauthorizedOrderAccess() throws IOException {
        long createdOrderId = createMinimalOrder();
        System.out.println("SEC_TC_001 — Created order ID: " + createdOrderId);

        URL url = new URL(API_URL + "/api/order/" + createdOrderId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int statusCode = connection.getResponseCode();
        System.out.println("SEC_TC_001 — Status: " + statusCode);

        assertNotEquals(200, statusCode);
        assertTrue(statusCode == 401 || statusCode == 403);

        connection.disconnect();
    }

    // PERF_TC_001: Add to basket response time
    @Test
    public void testAddToBasketResponseTime() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try {
            long[] durations = new long[3];
            for (int run = 0; run < 3; run++) {
                driver.get(FRONTEND_URL + "/scoops-builder");
                WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
                wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));

                WebElement coneRadio = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".cone-item input[type='radio']")));
                coneRadio.click();
                WebElement flavourCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".flavour-item input[type='checkbox']")));
                flavourCheckbox.click();
                WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".green-button:not([disabled])")));

                long start = System.currentTimeMillis();
                addBtn.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".toast, .notification, [class*='toast'], [class*='success']")));

                durations[run] = System.currentTimeMillis() - start;
                System.out.printf("PERF_TC_001 Run %d: %d ms%n", run + 1, durations[run]);
                assertTrue(durations[run] <= 6000);
            }

            long average = (durations[0] + durations[1] + durations[2]) / 3;
            System.out.printf("PERF_TC_001 Average: %d ms%n", average);
            assertTrue(average <= 3000);

        } finally {
            driver.quit();
        }
    }

    // PERF_TC_002: Delivery estimate load time
    @Test
    public void testDeliveryEstimateResponseTime() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(FRONTEND_URL + "/scoops-builder");
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_READY_LOCATOR));

            WebElement coneRadio = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".cone-item input[type='radio']")));
            coneRadio.click();
            WebElement flavourCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".flavour-item input[type='checkbox']")));
            flavourCheckbox.click();
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".green-button:not([disabled])")));
            addBtn.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".toast, .notification, [class*='toast'], [class*='success']")));

            long[] durations = new long[3];
            for (int run = 0; run < 3; run++) {
                driver.get(FRONTEND_URL);
                Thread.sleep(500);
                long start = System.currentTimeMillis();
                driver.get(FRONTEND_URL + "/cart");

                WebDriverWait runWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                runWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(),'Est Delivery') or contains(text(),'minutes')]")));

                durations[run] = System.currentTimeMillis() - start;
                System.out.printf("PERF_TC_002 Run %d: %d ms%n", run + 1, durations[run]);
                assertTrue(durations[run] <= 1000);
            }

            long average = (durations[0] + durations[1] + durations[2]) / 3;
            System.out.printf("PERF_TC_002 Average: %d ms%n", average);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        } finally {
            driver.quit();
        }
    }

    // Helper: Create test order
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
        assertEquals(201, status);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        conn.disconnect();

        String response = sb.toString();
        int idx = response.indexOf("\"orderId\":");
        if (idx == -1) {
            idx = response.indexOf("\"id\":") + 5;
        } else {
            idx += 10;
        }
        while (idx < response.length() && !Character.isDigit(response.charAt(idx))) idx++;
        int end = idx;
        while (end < response.length() && Character.isDigit(response.charAt(end))) end++;

        return Long.parseLong(response.substring(idx, end));
    }
}