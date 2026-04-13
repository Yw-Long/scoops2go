package Scoops2Go.scoops2goapi.infrastructure;

import Scoops2Go.scoops2goapi.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
	//java.util.Optional<Order> findByOrderNumber(String orderNumber);
}
