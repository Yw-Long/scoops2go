package Scoops2Go.scoops2goapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CheckoutDTO (
        boolean paid,
        String transactionId,
        OrderDTO order,
        LocalDateTime estimatedDeliveryTime
) {}
