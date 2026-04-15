package scoops2Go.service;

import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.exception.InvalidTreatException;
import Scoops2Go.scoops2goapi.infrastructure.OrderRepository;
import Scoops2Go.scoops2goapi.infrastructure.PaymentGateway;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import Scoops2Go.scoops2goapi.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderServiceValidationTests {

    static class TestableOrderService extends OrderService {
        public TestableOrderService(OrderRepository r, ProductRepository p, PaymentGateway g) {
            super(r, p, g);
        }

        @Override
        public void validateTreatProducts(List<ProductDTO> productDTOs) {
            super.validateTreatProducts(productDTOs);
        }

        @Override
        public boolean validateBasketSize(int basketSize) {
            return super.validateBasketSize(basketSize);
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

    private ProductDTO makeProduct(String type) {
        return new ProductDTO(null, null, null, null, null, type);
    }

    private List<ProductDTO> buildTreat(int cones, int flavours, int toppings) {
        List<ProductDTO> list = new ArrayList<>();
        for (int i = 0; i < cones; i++)    list.add(makeProduct("CONE"));
        for (int i = 0; i < flavours; i++) list.add(makeProduct("FLAVOR"));
        for (int i = 0; i < toppings; i++) list.add(makeProduct("TOPPING"));
        return list;
    }

    // ── validateTreatProducts ─────────────────────────────────

    // TC_TC_006: 6 toppings → exception
    @Test
    void validateTreat_6toppings_throwsException() {
        assertThrows(InvalidTreatException.class,
                () -> orderService.validateTreatProducts(buildTreat(1, 1, 6)),
                "Expected exception for 6 toppings (max is 5)");
    }

    // TC_TC_007: 1 cone + 2 flavours + 3 toppings → passes
    @Test
    void validateTreat_1cone2flavour3topping_passes() {
        assertDoesNotThrow(
                () -> orderService.validateTreatProducts(buildTreat(1, 2, 3)));
    }

    // TC_TC_008: 1 cone + 3 flavours → passes (upper boundary of flavours)
    @Test
    void validateTreat_1cone3flavour_passes() {
        assertDoesNotThrow(
                () -> orderService.validateTreatProducts(buildTreat(1, 3, 0)));
    }

    // TC_TC_009: 1 cone + 1 flavour + 5 toppings → passes (upper boundary of toppings)
    @Test
    void validateTreat_1cone1flavour5toppings_passes() {
        assertDoesNotThrow(
                () -> orderService.validateTreatProducts(buildTreat(1, 1, 5)));
    }

    // ── validateBasketSize ────────────────────────────────────

    // BK_TC_003: 9 treats → true (valid)
    @Test
    void validateBasketSize_9_returnsTrue() {
        assertTrue(orderService.validateBasketSize(9),
                "Expected basket size 9 to be valid");
    }

    // BK_TC_004: 11 treats → false (invalid, exceeds max of 9)
    @Test
    void validateBasketSize_11_returnsFalse() {
        assertFalse(orderService.validateBasketSize(11),
                "Expected basket size 11 to be invalid");
    }
}