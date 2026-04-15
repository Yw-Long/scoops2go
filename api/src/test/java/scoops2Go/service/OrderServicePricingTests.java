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

    // Store coordinates (REQ-BR-003, Appendix C) — units: radians
    private static final double STORE_LAT    =  0.9332368012864035;
    private static final double STORE_LNG    = -0.03907303503024745;
    private static final double DELIVERY_LAT =  0.9330151706862456;
    private static final double DELIVERY_LNG = -0.03993602204988841;

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double D_MAX_KM        = 30.0;
    private static final int    P_MAX           = 10;

    /**
     * Reference implementation of the equirectangular distance formula (Appendix C).
     * Coordinates are in radians as specified.
     */
    private static double calcDistanceKm(double storeLat, double storeLng,
                                         double deliveryLat, double deliveryLng) {
        double x = (deliveryLng - storeLng) * Math.cos((storeLat + deliveryLat) / 2.0);
        double y = deliveryLat - storeLat;
        return EARTH_RADIUS_KM * Math.sqrt(x * x + y * y);
    }

    /**
     * Reference implementation of the delivery time formula (Appendix C).
     *
     * CORRECTED formula:
     *   t = 900 + 2100 × ( p/(2×pMax) + d/(2×dMax) )   [seconds]
     *
     * NOTE: d is linear (not squared). The previous version used d²/dMax which
     * does not match Appendix C. Result is converted to minutes and rounded.
     */
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

    // BK_TC_005 — REQ-BR-002: Non-summer month (March) → £0.00 surcharge
    @Test
    void calcSurcharge_nonSummerDate_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)),
                "REQ-BR-002: Expected £0.00 surcharge in March (outside summer window).");
    }

    // BK_TC_006 — REQ-BR-002: 7 Sep (inclusive upper boundary) → £3.00
    // DEFECT: service uses date.isBefore(Sep 7), excluding this date; fix with !date.isAfter(end)
    @Test
    void calcSurcharge_sep7_returnsSurcharge() {
        LocalDateTime dt = LocalDateTime.of(2024, 9, 7, 12, 0);
        assertEquals(0, BigDecimal.valueOf(3.00).compareTo(orderService.calcSurcharge(dt)),
                "REQ-BR-002 [DEFECT]: 7 Sep is inclusive upper boundary; expected £3.00.");
    }

    // BK_TC_007 — REQ-BR-003: Appendix C worked example → 22 minutes
// DEFECT: service ignores coordinates and uses a simplified formula instead of REQ-BR-003.
    @Test
    void calcEstDeliveryMinutes_WorkedExample_returns22() {
        int expected = calcExpectedDeliveryMinutes(3, DELIVERY_LAT, DELIVERY_LNG);

        // Sanity-check: reference formula must yield 22 min
        assertEquals(22, expected,
                "Reference sanity check failed: Appendix C worked example should yield 22 min, got " + expected + ".");

        // Production method should also return 22, but DEFECT causes wrong result
        int actual = orderService.calcEstDeliveryMinutesActual(3, 3);
        assertEquals(22, actual,
                "DEFECT [REQ-BR-003]: Service returned " + actual + " min but expected 22 min. " +
                        "Production code must use the distance-based formula per Appendix C.");
    }

    // BK_TC_011 — REQ-BR-002: 1 Jun (inclusive lower boundary) → £3.00
    @Test
    void calcSurcharge_jun1_returnsSurcharge() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 1, 12, 0);
        assertEquals(0, BigDecimal.valueOf(3.00).compareTo(orderService.calcSurcharge(dt)),
                "REQ-BR-002: Expected £3.00 surcharge on 1 June.");
    }

    // BK_TC_012 — REQ-BR-002: 31 May (day before summer window) → £0.00
    @Test
    void calcSurcharge_may31_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 5, 31, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)),
                "REQ-BR-002: Expected £0.00 surcharge on 31 May.");
    }

    // BK_TC_013 — REQ-BR-002: 8 Sep (day after summer window) → £0.00
    @Test
    void calcSurcharge_sep8_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 9, 8, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)),
                "REQ-BR-002: Expected £0.00 surcharge on 8 September.");
    }
}