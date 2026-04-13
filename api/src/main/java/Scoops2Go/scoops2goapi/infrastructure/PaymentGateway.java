package Scoops2Go.scoops2goapi.infrastructure;

import Scoops2Go.scoops2goapi.dto.OrderDTO;
import Scoops2Go.scoops2goapi.dto.PaymentDTO;

public interface PaymentGateway {

    PaymentDTO processPayment(OrderDTO orderDTO);
}
