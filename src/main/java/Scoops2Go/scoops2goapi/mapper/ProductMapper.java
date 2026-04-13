package Scoops2Go.scoops2goapi.mapper;

import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.model.Product;
import Scoops2Go.scoops2goapi.model.ProductType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ProductMapper {

    //private ProductMapper() {}

    public static ProductDTO toDto(Product p) {
        if (p == null) return null;
        return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getDescription(),
                p.getIngredients(),
                p.getProductType().name()
        );
    }

    public static List<ProductDTO> toDtoList(List<Product> products) {
        if (products == null || products.isEmpty()) return List.of();
        return products.stream()
                .filter(Objects::nonNull)
                .map(ProductMapper::toDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public static Product toEntity(ProductDTO pd) {
        //Long id = pd.productId();
        String name = pd.productName();
        BigDecimal price = pd.price() != null ? pd.price() : BigDecimal.ZERO;
        String description = pd.description();
        List<String> ingredients = pd.ingredients() == null ? List.of() : List.copyOf(pd.ingredients());

        ProductType type = null;
        if (pd.type() != null) {
            try {
                type = ProductType.valueOf(pd.type());
            } catch (IllegalArgumentException ex) {
                // unknown product type string -> keep null (or handle mapping rules here)
            }
        }

        return new Product(name, price, description, ingredients, type);
    }
}
