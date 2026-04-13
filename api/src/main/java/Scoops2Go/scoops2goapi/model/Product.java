package Scoops2Go.scoops2goapi.model;

import jakarta.persistence.*;
import java.util.List;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
    @SequenceGenerator(name = "products_seq", sequenceName = "products_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    // precision/scale chosen for monetary values; adjust to fit your DB/currency rules
    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(length = 2000)
    private String description;

    // Ingredients stored as a simple element collection.
    // This creates a product_ingredients table with (product_id, ingredient) rows.
    @ElementCollection
    @CollectionTable(name = "product_ingredients", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "ingredient", length = 500)
    private List<String> ingredients = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 50)
    private ProductType productType;

    @Version
    private Long version;

    protected Product() {
        // JPA
    }

    public Product(String name, BigDecimal price, String description, List<String> ingredients, ProductType productType) {
        //this.id = productId;
        this.name = name;
        this.price = price;
        this.description = description;
        setIngredients(ingredients);
        this.productType = productType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getIngredients() {
        return List.copyOf(ingredients);
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = (ingredients == null) ? new ArrayList<>() : new ArrayList<>(ingredients);
    }

    public void addIngredient(String ingredient) {
        if (ingredient != null) {
            this.ingredients.add(ingredient);
        }
    }

    public void removeIngredient(String ingredient) {
        this.ingredients.remove(ingredient);
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public Long getVersion() {
        return version;
    }

    // equals/hashCode: use id when available; avoid collections in equality
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product other = (Product) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", productType=" + productType +
                '}';
    }
}