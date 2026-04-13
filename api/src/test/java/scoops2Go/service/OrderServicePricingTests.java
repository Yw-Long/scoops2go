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

        @Override
        public int calcEstDeliveryMinutes(int treatCount, int productCount) {
            return super.calcEstDeliveryMinutes(treatCount, productCount);
        }
    }

    private TestableOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new TestableOrderService(
                mock(OrderRepository.class),
                mock(ProductRepository.class),
                mock(PaymentGateway.class)
        );
    }

    // BK_TC_005: non-summer date → £0.00
    @Test
    void calcSurcharge_nonSummerDate_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)),
                "Expected £0.00 surcharge in March");
    }

    // BK_TC_006: Sep 7 → should be £3.00 per requirements
    @Test
    void calcSurcharge_sep7_expectsSurcharge_bugFound() {
        LocalDateTime dt = LocalDateTime.of(2024, 9, 7, 12, 0);
        assertEquals(0, BigDecimal.valueOf(3.00).compareTo(orderService.calcSurcharge(dt)),
                "BUG: Sep 7 should incur £3.00 surcharge but isBefore(Sep7) excludes it");
    }

    // BK_TC_007: 1 treat, 2 products → 20 + (2*1) + ceil(0.4*2) = 20+2+1 = 23 mins
    @Test
    void calcEstDeliveryMinutes_1treat_2products_returns23() {
        int result = orderService.calcEstDeliveryMinutes(1, 2);
        assertEquals(23, result,
                "Expected 23 minutes for 1 treat and 2 products");
    }

    // BK_TC_011: Jun 1 → £3.00 (summer start boundary)
    @Test
    void calcSurcharge_jun1_returnsSurcharge() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 1, 12, 0);
        assertEquals(0, BigDecimal.valueOf(3.00).compareTo(orderService.calcSurcharge(dt)),
                "Expected £3.00 surcharge on Jun 1 (summer start)");
    }

    // BK_TC_012: May 31 → £0.00 (day before summer starts)
    @Test
    void calcSurcharge_may31_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 5, 31, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)),
                "Expected £0.00 surcharge on May 31 (before summer)");
    }

    // BK_TC_013: Sep 8 → £0.00 (day after summer ends)
    @Test
    void calcSurcharge_sep8_returnsZero() {
        LocalDateTime dt = LocalDateTime.of(2024, 9, 8, 12, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(orderService.calcSurcharge(dt)),
                "Expected £0.00 surcharge on Sep 8 (after summer)");
    }
}