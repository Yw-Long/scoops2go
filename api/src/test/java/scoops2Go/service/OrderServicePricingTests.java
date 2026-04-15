package scoops2Go.service;

import Scoops2Go.scoops2goapi.infrastructure.OrderRepository;
import Scoops2Go.scoops2goapi.infrastructure.PaymentGateway;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import Scoops2Go.scoops2goapi.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderServicePricingTests {

    static class TestableOrderService extends OrderService {

        public TestableOrderService(OrderRepository r, ProductRepository p, PaymentGateway g) {
            super(r, p, g);
        }

        @Override
        public BigDecimal calcSurcharge(LocalDateTime dateTime) {
            return super.calcSurcharge(dateTime);
        }

        public int calcEstDeliveryMinutesActual(int treatCount, int productCount) {
            return super.calcEstDeliveryMinutes(treatCount, productCount);
        }
    }

    private TestableOrderService orderService;

    // Store and delivery coordinates (radians)
    private static final double STORE_LAT    =  0.9332368012864035;
    private static final double STORE_LNG    = -0.03907303503024745;
    private static final double DELIVERY_LAT =  0.9330151706862456;
    private static final double DELIVERY_LNG = -0.03993602204988841;

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double D_MAX_KM        = 30.0;
    private static final int    P_MAX           = 10;

    // Calculate distance (equirectangular formula)
    private static double calcDistanceKm(double storeLat, double storeLng,
                                         double deliveryLat, double deliveryLng) {
        double x = (deliveryLng - storeLng) * Math.cos((storeLat + deliveryLat) / 2.0);
        double y = deliveryLat - storeLat;
        return EARTH_RADIUS_KM * Math.sqrt(x * x + y * y);
    }

    // Calculate expected delivery time
    private static int calcExpectedDeliveryMinutes(int p, double deliveryLat, double deliveryLng) {
        double d = calcDistanceKm(STORE_LAT, STORE_LNG, deliveryLat, deliveryLng);
        double tSeconds = 900.0
                + 2100.0 * ((double) p / (2.0 * P_MAX) + d / (2.0 * D_MAX_KM));
        return (int) Math.round(tSeconds / 60.0);
    }

    @BeforeEach
    void setUp() {
        orderService = new TestableOrderService(
                mock(OrderRepository.class),
                mock(ProductRepository.class),
                mock(PaymentGateway.class)
        );
    }

    // BK_TC_005: Non-summer month → £0.00
    @Test
    void calcSurcharge_nonSummerDate_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)));
    }

    // BK_TC_006: Sep 7 → £3.00 (defect exists)
    @Test
    void calcSurcharge_sep7_returnsSurcharge() {
        LocalDateTime dt = LocalDateTime.of(2024, 9, 7, 12, 0);
        assertEquals(0, BigDecimal.valueOf(3.00).compareTo(orderService.calcSurcharge(dt)));
    }

    // BK_TC_007: Delivery time should return 22 min (defect exists)
    @Test
    void calcEstDeliveryMinutes_WorkedExample_returns22() {
        int expected = calcExpectedDeliveryMinutes(3, DELIVERY_LAT, DELIVERY_LNG);
        assertEquals(22, expected);
        int actual = orderService.calcEstDeliveryMinutesActual(3, 3);
        assertEquals(22, actual);
    }

    // BK_TC_011: Jun 1 → £3.00
    @Test
    void calcSurcharge_jun1_returnsSurcharge() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 1, 12, 0);
        assertEquals(0, BigDecimal.valueOf(3.00).compareTo(orderService.calcSurcharge(dt)));
    }

    // BK_TC_012: May 31 → £0.00
    @Test
    void calcSurcharge_may31_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 5, 31, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)));
    }

    // BK_TC_013: Sep 8 → £0.00
    @Test
    void calcSurcharge_sep8_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 9, 8, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)));
    }
}