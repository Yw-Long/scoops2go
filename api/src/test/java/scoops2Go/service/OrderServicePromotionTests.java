package scoops2Go.service;

import Scoops2Go.scoops2goapi.exception.InvalidPromotionException;
import Scoops2Go.scoops2goapi.infrastructure.OrderRepository;
import Scoops2Go.scoops2goapi.infrastructure.PaymentGateway;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import Scoops2Go.scoops2goapi.model.Order;
import Scoops2Go.scoops2goapi.model.Treat;
import Scoops2Go.scoops2goapi.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderServicePromotionTests {

    static class TestableOrderService extends OrderService {
        public TestableOrderService(OrderRepository r, ProductRepository p, PaymentGateway g) {
            super(r, p, g);
        }

        @Override
        public void luckyForSome(Order order) {
            super.luckyForSome(order);
        }

        @Override
        public void megaMelt100(Order order) {
            super.megaMelt100(order);
        }

        @Override
        public void frozen40(Order order) {
            super.frozen40(order);
        }

        @Override
        public void tripleTreat3(Order order) {
            super.tripleTreat3(order);
        }

        @Override
        public void applyPromotion(Order order, String promotion) {
            super.applyPromotion(order, promotion);
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

    private Order orderWithTotal(double total) {
        Order o = new Order(null, BigDecimal.valueOf(total), BigDecimal.ZERO, 0, null);
        o.setTreats(new ArrayList<>());
        return o;
    }

    private Order orderWithTotalAndTreats(double total, int treatCount) {
        Order o = new Order(null, BigDecimal.valueOf(total), BigDecimal.ZERO, 0, null);
        List<Treat> treats = new ArrayList<>();
        for (int i = 0; i < treatCount; i++) treats.add(new Treat());
        o.setTreats(treats);
        return o;
    }

    // PR_TC_001: Apply discount for total > £13
    @Test
    void luckyForSome_above13_appliesDiscount() {
        Order o = orderWithTotal(14.50);
        orderService.luckyForSome(o);
        assertTrue(o.getOrderTotal().compareTo(BigDecimal.valueOf(14.50)) < 0);
    }

    // PR_TC_002: Apply discount for total = £13
    @Test
    void luckyForSome_exactly13_appliesDiscount() {
        Order o = orderWithTotal(13.00);
        orderService.luckyForSome(o);
        assertTrue(o.getOrderTotal().compareTo(BigDecimal.valueOf(13.00)) < 0);
    }

    // PR_TC_003: Reject total = £12.98
    @Test
    void luckyForSome_12_98_throwsException() {
        Order o = orderWithTotal(12.98);
        assertThrows(InvalidPromotionException.class, () -> orderService.luckyForSome(o));
    }

    // PR_TC_004: Reject total = £12.99
    @Test
    void luckyForSome_12_99_throwsException() {
        Order o = orderWithTotal(12.99);
        assertThrows(InvalidPromotionException.class, () -> orderService.luckyForSome(o));
    }

    // PR_TC_005: Apply £20 discount for total = £150
    @Test
    void megaMelt100_150_appliesDiscount() {
        Order o = orderWithTotal(150.00);
        orderService.megaMelt100(o);
        assertEquals(0, new BigDecimal("130.00").compareTo(o.getOrderTotal()));
    }

    // PR_TC_006: Reject total = £100 (bug exists)
    @Test
    void megaMelt100_exactly100_shouldReject_butBugAllowsIt() {
        Order o = orderWithTotal(100.00);
        assertThrows(InvalidPromotionException.class, () -> orderService.megaMelt100(o));
    }

    // PR_TC_007: Apply £20 discount for total = £100.01
    @Test
    void megaMelt100_100_01_appliesDiscount() {
        Order o = orderWithTotal(100.01);
        orderService.megaMelt100(o);
        assertEquals(0, new BigDecimal("80.01").compareTo(o.getOrderTotal()));
    }

    // PR_TC_008: Apply 40% discount for 4 treats & total = £40
    @Test
    void frozen40_4treats_40pounds_appliesDiscount() {
        Order o = orderWithTotalAndTreats(40.00, 4);
        orderService.frozen40(o);
        assertEquals(0, new BigDecimal("24.00").compareTo(o.getOrderTotal()));
    }

    // PR_TC_009: Reject 3 treats
    @Test
    void frozen40_3treats_throwsException() {
        Order o = orderWithTotalAndTreats(50.00, 3);
        assertThrows(InvalidPromotionException.class, () -> orderService.frozen40(o));
    }

    // PR_TC_010: Reject 4 treats & total = £39.99
    @Test
    void frozen40_4treats_39_99_throwsException() {
        Order o = orderWithTotalAndTreats(39.99, 4);
        assertThrows(InvalidPromotionException.class, () -> orderService.frozen40(o));
    }

    // PR_TC_011: Apply discount for 4 treats & total = £40
    @Test
    void frozen40_4treats_exactly40_appliesDiscount() {
        Order o = orderWithTotalAndTreats(40.00, 4);
        orderService.frozen40(o);
        assertTrue(o.getOrderTotal().compareTo(BigDecimal.valueOf(40.00)) < 0);
    }

    // PR_TC_012: Apply £3 discount for 3 treats
    @Test
    void tripleTreat3_exactly3treats_appliesDiscount() {
        Order o = orderWithTotalAndTreats(20.00, 3);
        orderService.tripleTreat3(o);
        assertEquals(0, new BigDecimal("17.00").compareTo(o.getOrderTotal()));
    }

    // PR_TC_014: Apply £3 discount for 4 treats
    @Test
    void tripleTreat3_4treats_appliesDiscount() {
        Order o = orderWithTotalAndTreats(20.00, 4);
        orderService.tripleTreat3(o);
        assertEquals(0, new BigDecimal("17.00").compareTo(o.getOrderTotal()));
    }

    // PR_TC_017: Reject empty promotion code
    @Test
    void applyPromotion_emptyString_throwsException() {
        Order o = orderWithTotal(50.00);
        assertThrows(InvalidPromotionException.class, () -> orderService.applyPromotion(o, ""));
    }

    // PR_TC_019: Reject invalid case promotion
    @Test
    void applyPromotion_wrongCase_throwsException() {
        Order o = orderWithTotal(50.00);
        assertThrows(InvalidPromotionException.class, () -> orderService.applyPromotion(o, "luckyforsOme"));
    }
}