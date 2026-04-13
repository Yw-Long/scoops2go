package Scoops2Go.scoops2goapi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Order JPA entity.
 *
 * - id is Long (nullable before persist) although DTO uses int; mapper converts safely.
 * - treats is a lazy OneToMany mapped by Treat.order
 * - estDeliveryMinutes stored as Integer so it can be null in the DB
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    @SequenceGenerator(name = "orders_seq", sequenceName = "orders_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "order_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal orderTotal;

    @Column(name = "delivery_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal deliveryCost;

    @Column(name = "est_delivery_minutes")
    private Integer estDeliveryMinutes;

    // Keep promotion as a simple string (code/name). Replace with an entity reference if you have one.
    @Column(name = "promotion")
    private String promotion;

    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Treat> treats = new ArrayList<>();

    protected Order() {
        // JPA
    }

    public Order(LocalDateTime orderTime,
                 BigDecimal orderTotal,
                 BigDecimal deliveryCost,
                 Integer estDeliveryMinutes,
                 String promotion) {
        this.orderTime = orderTime;
        this.orderTotal = orderTotal;
        this.deliveryCost = deliveryCost;
        this.estDeliveryMinutes = estDeliveryMinutes;
        this.promotion = promotion;
    }

    // --- getters / setters ---

    public Long getId() {
        return id;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(BigDecimal deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public Integer getEstDeliveryMinutes() {
        return estDeliveryMinutes;
    }

    public void setEstDeliveryMinutes(Integer estDeliveryMinutes) {
        this.estDeliveryMinutes = estDeliveryMinutes;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public List<Treat> getTreats() {
        return List.copyOf(treats);
    }

    public void setTreats(List<Treat> treats) {
        this.treats.clear();
        if (treats != null && !treats.isEmpty()) {
            treats.forEach(this::addTreat);
        }
    }

    // convenience helpers to maintain bidirectional relationship
    public void addTreat(Treat t) {
        if (t == null) return;
        treats.add(t);
        //t.setOrder(this);
    }

    public void removeTreat(Treat t) {
        if (t == null) return;
        treats.remove(t);
        //t.setOrder(null);
    }

    public Long getVersion() {
        return version;
    }

    // equals/hashCode using id only (null-safe)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order other = (Order) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderTime=" + orderTime +
                ", orderTotal=" + orderTotal +
                ", deliveryCost=" + deliveryCost +
                '}';
    }
}