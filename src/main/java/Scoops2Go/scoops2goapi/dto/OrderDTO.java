package Scoops2Go.scoops2goapi.dto;

//+ orderId
//+ orderTime
//+ orderTotal
//+ deliveryCost
//+ estDeliveryMinutes
//+ promotion
//+ basketItems

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO (
        long orderId,
        LocalDateTime orderTime,
        BigDecimal orderTotal,
        BigDecimal deliveryCost,
        int estDeliveryMinutes,
        String promotion,
        List<TreatDTO> basketItems
) {}
