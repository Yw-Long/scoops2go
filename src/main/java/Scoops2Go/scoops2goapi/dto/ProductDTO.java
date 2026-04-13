package Scoops2Go.scoops2goapi.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDTO(
        Long productId,
        String productName,
        BigDecimal price,
        String description,
        List<String> ingredients,
        String type
) {}
