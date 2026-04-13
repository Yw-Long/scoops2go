package Scoops2Go.scoops2goapi.model;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Flavor extends Product {
    public Flavor() {
        super();
        setProductType(ProductType.FLAVOR);
    }

    public Flavor(String name, BigDecimal price, String description, java.util.List<String> ingredients) {
        super(name, price, description, ingredients, ProductType.FLAVOR);
    }
}
