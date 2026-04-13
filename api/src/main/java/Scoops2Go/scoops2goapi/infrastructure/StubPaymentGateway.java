package Scoops2Go.scoops2goapi.infrastructure;

import Scoops2Go.scoops2goapi.dto.OrderDTO;
import Scoops2Go.scoops2goapi.dto.PaymentDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StubPaymentGateway implements PaymentGateway {

    @Override
    public PaymentDTO processPayment(OrderDTO orderDTO) {
        String tx = UUID.randomUUID().toString();
        return new PaymentDTO(true, tx, "Payment successful.");
    }
}
