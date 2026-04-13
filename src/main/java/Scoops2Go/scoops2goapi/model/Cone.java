package Scoops2Go.scoops2goapi.model;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Cone extends Product {
    public Cone() {
        super();
        setProductType(ProductType.CONE);
    }

    public Cone(String name, BigDecimal price, String description, java.util.List<String> ingredients) {
        super(name, price, description, ingredients, ProductType.CONE);
    }
}
