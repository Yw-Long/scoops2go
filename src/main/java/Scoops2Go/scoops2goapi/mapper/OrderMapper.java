package Scoops2Go.scoops2goapi.mapper;

import Scoops2Go.scoops2goapi.dto.OrderDTO;
import Scoops2Go.scoops2goapi.dto.TreatDTO;
import Scoops2Go.scoops2goapi.model.Order;
import Scoops2Go.scoops2goapi.model.Treat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class OrderMapper {

    private OrderMapper() {}

    public static OrderDTO toDto(Order o) {
        if (o == null) return null;

        int orderId = (o.getId() == null) ? 0 : o.getId().intValue();
        int estMinutes = (o.getEstDeliveryMinutes() == null) ? 0 : o.getEstDeliveryMinutes();

        List<TreatDTO> basketItems = TreatMapper.toDtoList(o.getTreats());

        return new OrderDTO(
                orderId,
                o.getOrderTime(),
                o.getOrderTotal(),
                o.getDeliveryCost(),
                estMinutes,
                o.getPromotion(),
                basketItems
        );
    }

    public static List<OrderDTO> toDtoList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return List.of();
        return orders.stream()
                .filter(Objects::nonNull)
                .map(OrderMapper::toDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public static Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) return null;

        LocalDateTime orderTime = orderDTO.orderTime() != null ? orderDTO.orderTime() : LocalDateTime.now();
        BigDecimal orderTotal = orderDTO.orderTotal() != null ? orderDTO.orderTotal() : BigDecimal.ZERO;
        BigDecimal deliveryCost = orderDTO.deliveryCost() != null ? orderDTO.deliveryCost() : BigDecimal.ZERO;
        Integer estMinutes = orderDTO.estDeliveryMinutes(); // dto primitive -> boxed by record accessor
        String promotion = orderDTO.promotion();

        Order orderEntity = new Order(orderTime, orderTotal, deliveryCost, estMinutes, promotion);

        List<TreatDTO> treatDTOs = orderDTO.basketItems();
        if (treatDTOs == null || treatDTOs.isEmpty())
            return orderEntity;

        List<Treat> treatEntities = TreatMapper.toEntityList(treatDTOs);
        orderEntity.setTreats(treatEntities);

        return orderEntity;
    }
}
