package Scoops2Go.scoops2goapi.model;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Topping extends Product {
    public Topping() {
        super();
        setProductType(ProductType.TOPPING);
    }

    public Topping(String name, BigDecimal price, String description, java.util.List<String> ingredients) {
        super(name, price, description, ingredients, ProductType.TOPPING);
    }
}
