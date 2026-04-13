package Scoops2Go.scoops2goapi.service;

import Scoops2Go.scoops2goapi.dto.CheckoutDTO;
import Scoops2Go.scoops2goapi.dto.OrderDTO;
import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.dto.TreatDTO;
import Scoops2Go.scoops2goapi.exception.*;
import Scoops2Go.scoops2goapi.infrastructure.PaymentGateway;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import Scoops2Go.scoops2goapi.mapper.OrderMapper;
import Scoops2Go.scoops2goapi.model.Order;
import Scoops2Go.scoops2goapi.model.Product;
import Scoops2Go.scoops2goapi.model.Treat;
import Scoops2Go.scoops2goapi.infrastructure.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Scoops2Go.scoops2goapi.model.ProductType.TOPPING;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.paymentGateway = paymentGateway;
    }

    // crud operations
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        return OrderMapper.toDto(order);
    }

    public OrderDTO createOrder(OrderDTO incomingOrder) {
        Order order = OrderMapper.toEntity(incomingOrder);

        // Build Treats from incoming DTO using productIds and attach managed Product entities
        List<Treat> treats = new ArrayList<>();
        List<TreatDTO> treatDTOs = incomingOrder.basketItems();
        if (treatDTOs != null) {
            for (TreatDTO td : treatDTOs) {
                Treat t = new Treat();
                t.setOrder(order);

                List<ProductDTO> pdList = td.products();
                List<Product> attached = (pdList == null ? List.<ProductDTO>of() : pdList).stream()
                        .filter(Objects::nonNull)
                        .map((ProductDTO pd) -> {
                            Long pid = pd.productId(); // productId is canonical; @JsonAlias("id") can map { id } as well
                            if (pid == null)
                                throw new ResourceNotFoundException("Product id is required for all items");
                            return productRepository.findById(pid)
                                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + pid));
                        })
                        .collect(Collectors.toList());
                t.setProducts(attached);
                treats.add(t);
            }
        }
        order.setTreats(treats);

        // calc basketSize (treatCount) + validate basket
        int basketSize = order.getTreats().size();
        boolean isValidBasket = validateBasketSize(basketSize);
        if (!isValidBasket)
            throw new InvalidBasketException("Invalid basket size: " + basketSize, basketSize);

        // validate treats
        if (treatDTOs != null)
            for (TreatDTO td : treatDTOs)
                validateTreatProducts(td.products());

        // calc costs
        BigDecimal subTotal = calcSubtotal(order.getTreats());
        BigDecimal standardDelivery = BigDecimal.valueOf(2.50);
        BigDecimal seasonalSurcharge = calcSurcharge(order.getOrderTime());
        BigDecimal totalDeliveryCost = standardDelivery.add(seasonalSurcharge);
        BigDecimal orderTotalCost = subTotal.add(totalDeliveryCost);

        // calc delivery est.
        int productCount = order.getTreats() == null ? 0 : order.getTreats().stream()
                .filter(Objects::nonNull)
                .mapToInt(t -> t.getProducts() == null ? 0 : t.getProducts().size())
                .sum();

        int estimatedDeliveryMinutes = calcEstDeliveryMinutes(basketSize, productCount);

        // assign calc values to order entity
        order.setDeliveryCost(totalDeliveryCost);
        order.setOrderTotal(orderTotalCost);
        order.setEstDeliveryMinutes(estimatedDeliveryMinutes);

        Order outgoingOrder = orderRepository.save(order);

        return OrderMapper.toDto(outgoingOrder);
    }

    public OrderDTO updateOrder(OrderDTO orderDTO) {
        long id = orderDTO.orderId();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        // update basket items
        List<Treat> treats = new ArrayList<>();
        List<TreatDTO> treatDTOs = orderDTO.basketItems();
        if (treatDTOs != null) {
            for (TreatDTO td : treatDTOs) {
                Treat t = new Treat();
                t.setOrder(order);

                List<ProductDTO> pdList = td.products();
                List<Product> attached = (pdList == null ? List.<ProductDTO>of() : pdList).stream()
                        .filter(Objects::nonNull)
                        .map((ProductDTO pd) -> {
                            Long pid = pd.productId(); // productId is canonical; @JsonAlias("id") can map { id } as well
                            if (pid == null)
                                throw new ResourceNotFoundException("Product id is required for all items");
                            return productRepository.findById(pid)
                                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + pid));
                        })
                        .collect(Collectors.toList());
                t.setProducts(attached);
                treats.add(t);
            }
        }
        order.setTreats(treats);

        // validate treats
        if (treatDTOs != null)
            for (TreatDTO td : treatDTOs)
                validateTreatProducts(td.products());

        // calc basketSize (treatCount) + validate basket
        int basketSize = order.getTreats().size();
        boolean isValidBasket = validateBasketSize(basketSize);
        if (!isValidBasket)
            throw new InvalidBasketException("Invalid basket size: " + basketSize, basketSize);

        // calc costs
        BigDecimal subTotal = calcSubtotal(order.getTreats());
        BigDecimal standardDelivery = BigDecimal.valueOf(2.50);
        BigDecimal seasonalSurcharge = calcSurcharge(order.getOrderTime());
        BigDecimal totalDeliveryCost = standardDelivery.add(seasonalSurcharge);
        BigDecimal orderTotalCost = subTotal.add(totalDeliveryCost);

        // calc delivery est.
        int productCount = order.getTreats() == null ? 0 : order.getTreats().stream()
                .filter(Objects::nonNull)
                .mapToInt(t -> t.getProducts() == null ? 0 : t.getProducts().size())
                .sum();

        int estimatedDeliveryMinutes = calcEstDeliveryMinutes(basketSize, productCount);

        // assign calc values to order entity
        order.setDeliveryCost(totalDeliveryCost);
        order.setOrderTotal(orderTotalCost);
        order.setEstDeliveryMinutes(estimatedDeliveryMinutes);

        Order outgoingOrder = orderRepository.save(order);

        // apply promotion (if valid)
        if (!(orderDTO.promotion() == null || orderDTO.promotion().isBlank())) {
            applyPromotion(outgoingOrder, orderDTO.promotion());
            outgoingOrder.setPromotion(orderDTO.promotion());
        }

       outgoingOrder = orderRepository.save(outgoingOrder);

        return OrderMapper.toDto(outgoingOrder);
    }

    public CheckoutDTO checkoutOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        var paymentResponse = paymentGateway.processPayment(OrderMapper.toDto(order));

        if (!paymentResponse.success())
            throw new PaymentFailedException(paymentResponse.message());

        return new CheckoutDTO(
                true,
                paymentResponse.transactionId(),
                OrderMapper.toDto(order),
                calcEstimatedDeliveryTime(order.getOrderTime(), order.getEstDeliveryMinutes()));
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id))
            throw new ResourceNotFoundException("Order not found with id " + id);

        orderRepository.deleteById(id);
    }

    // business logic
    protected BigDecimal calcSubtotal(List<Treat> treats) {
        if (treats == null) return BigDecimal.ZERO;

        return treats.stream()
                .filter(Objects::nonNull)
                .flatMap(t -> t.getProducts() == null ? Stream.empty() : t.getProducts().stream())
                .filter(Objects::nonNull)
                .map(p -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected BigDecimal calcSurcharge(LocalDateTime dateTime) {
        BigDecimal seasonal = BigDecimal.valueOf(0.00);

        if (dateTime != null) {
            LocalDate date = dateTime.toLocalDate();
            int year = date.getYear();
            LocalDate start = LocalDate.of(year, 6, 1);
            LocalDate end = LocalDate.of(year, 9, 7);
            if (((date.isEqual(start) || date.isAfter(start)) && date.isBefore(end))) {
                seasonal = BigDecimal.valueOf(3.00);
            }
        }

        return seasonal;
    }

    protected int calcEstDeliveryMinutes(int treatCount, int productCount) {
        int base = 20;
        int perTreat = 2;
        double perProduct = 0.4;
        double total = base + (perTreat * Math.max(0, treatCount)) + (perProduct * Math.max(0, productCount));
        return (int) Math.ceil(total);
    }

    protected boolean validateBasketSize(int basketSize) {
        return basketSize < 10;
    }

    protected LocalDateTime calcEstimatedDeliveryTime(LocalDateTime orderTime, int estDeliveryMinutes) {
        return orderTime.plusMinutes(estDeliveryMinutes);
    }

    protected void validateTreatProducts(List<ProductDTO> productDTOs) {
        if (productDTOs == null || productDTOs.isEmpty())
            throw new InvalidTreatException("Treat must contain at least one product (cone + flavours/toppings)");

        int coneCount = 0;
        int flavourCount = 0;
        int toppingCount = 0;
        for (var p : productDTOs) {
            if (p == null || p.type() == null) continue;
            switch (p.type()) {
                case "CONE": coneCount++; break;
                case "FLAVOR": flavourCount++; break;
                case "TOPPING": toppingCount++; break;
            }
        }

        if (coneCount < 1) throw new InvalidTreatException("Treat must contain exactly 1 cone");
        if (coneCount > 1) throw new InvalidTreatException("Treat cannot contain more than 1 cone");
        if (flavourCount < 1) throw new InvalidTreatException("Treat must contain at least 1 flavour");
        if (flavourCount > 3) throw new InvalidTreatException("Treat cannot contain more than 3 flavours");
        if (toppingCount > 5) throw new InvalidTreatException("Treat cannot contain more than 5 toppings");
    }

    protected void applyPromotion(Order order, String promotion) {
        switch (promotion) {
            case "LuckyForSome":
                luckyForSome(order);
                break;
            case "MegaMelt100":
                megaMelt100(order);
                break;
            case "Frozen40":
                frozen40(order);
                break;
            case "TripleTreat3":
                tripleTreat3(order);
                break;
            case "ScoopThereItIs!":
                // e.g., free extra scoop
                scoopThereItIs(order);
                break;
            default:
                throw new InvalidPromotionException(
                        "Unknown promotion: " + promotion + "."
                );
        }
    }

    protected void luckyForSome (Order order){
        if (order == null || order.getOrderTotal() == null) return;

        final int MONEY_SCALE = 2; // typical currency scale
        final BigDecimal THIRTEEN = BigDecimal.valueOf(13);
        final BigDecimal THIRTEEN_PERCENT = BigDecimal.valueOf(0.13);

        BigDecimal totalCost = order.getOrderTotal();

        if (THIRTEEN.compareTo(totalCost) <= 0) {
            BigDecimal reduceBy = totalCost.multiply(THIRTEEN_PERCENT);
            BigDecimal newTotal = totalCost.subtract(reduceBy);
            order.setOrderTotal(newTotal);
        } else {
            throw new InvalidPromotionException(
                    "LuckyForSome can only be applied when total cost is greater than or equal to 13.00 GBP.S"
            );
        }
    }

    protected void megaMelt100(Order order) {
        if (order == null || order.getOrderTotal() == null) return;

        final BigDecimal THRESHOLD = new BigDecimal("100.00");
        final BigDecimal DISCOUNT  = new BigDecimal("20.00");
        final int MONEY_SCALE = 2;

        BigDecimal total = order.getOrderTotal();

        if (total.compareTo(THRESHOLD) >= 0) {
            BigDecimal newTotal = total.subtract(DISCOUNT)
                    .max(BigDecimal.ZERO) // optional safety: never go below 0
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            order.setOrderTotal(newTotal);
        } else {
            throw new InvalidPromotionException(
                    "MegaMelt100 can only be applied when total cost is greater than 100.00 GBP."
            );
        }
    }

    protected void frozen40(Order order) {
        if (order == null || order.getOrderTotal() == null) return;

        final BigDecimal THRESHOLD = new BigDecimal("39.99"); // since 40 or greater
        final BigDecimal FACTOR = new BigDecimal("0.60"); // 40% off => pay 60%
        final int MONEY_SCALE = 2;

        BigDecimal total = order.getOrderTotal();

        int treatCount = order.getTreats().size();

        // Apply only when there are at least 4 treats AND total >= threshold
        if (treatCount >= 4 && total.compareTo(THRESHOLD) >= 0) {
            BigDecimal newTotal = total.multiply(FACTOR)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            order.setOrderTotal(newTotal);
        } else {
            throw new InvalidPromotionException(
                    "Frozen40 can only be applied when total cost is 40.00 GBP or more and 4 or more treats are present in basket."
            );
        }
    }

    protected void tripleTreat3(Order order) {
        if (order == null || order.getOrderTotal() == null) return;

        final BigDecimal DISCOUNT = new BigDecimal("3.00");
        final int MONEY_SCALE = 2;

        int treatCount = order.getTreats().size();
        if (treatCount >= 3) {
            BigDecimal newTotal = order.getOrderTotal()
                    .subtract(DISCOUNT)
                    .max(BigDecimal.ZERO) // safety: don't go below zero
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            order.setOrderTotal(newTotal);
        } else {
            throw new InvalidPromotionException(
                    "TripleTreat3 can only be applied when 3 or more treats are present in the basket."
            );
        }
    }

    protected void scoopThereItIs(Order order) {
        if (order == null || order.getOrderTotal() == null) return;

        final BigDecimal DISCOUNT = new BigDecimal("1.00");
        final int MONEY_SCALE = 2;

        BigDecimal total = order.getOrderTotal();
        BigDecimal newTotal = total.subtract(DISCOUNT)
                .max(BigDecimal.ZERO)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        order.setOrderTotal(newTotal);
    }
}
