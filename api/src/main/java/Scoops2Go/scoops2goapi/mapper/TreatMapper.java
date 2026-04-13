package Scoops2Go.scoops2goapi.mapper;

import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.dto.TreatDTO;
import Scoops2Go.scoops2goapi.model.Product;
import Scoops2Go.scoops2goapi.model.Treat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TreatMapper {

    //private TreatMapper() {}

    public static TreatDTO toDto(Treat t) {
        if (t == null) return null;

        return new TreatDTO(
                // map nested Products using ProductMapper (produces unmodifiable list)
                ProductMapper.toDtoList(t.getProducts())
        );
    }

    public static List<TreatDTO> toDtoList(List<Treat> treats) {
        if (treats == null || treats.isEmpty()) return List.of();
        return treats.stream()
                .filter(Objects::nonNull)
                .map(TreatMapper::toDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public static Treat toEntity(TreatDTO treatDTO) {
        List<ProductDTO> productDTOs = treatDTO.products();

        if (productDTOs == null || productDTOs.isEmpty())
            return new Treat();

        List<Product> productEntities = new ArrayList<Product>();
        for (ProductDTO productDTO : productDTOs) {
            if (productDTO == null)
                continue;

            Product product = ProductMapper.toEntity(productDTO);
            productEntities.add(product);
        }

        Treat treatEntity = new Treat();
        treatEntity.setProducts(productEntities);

        return treatEntity;
    }

    public static List<Treat> toEntityList(List<TreatDTO> treatDTOs) {
        if (treatDTOs == null || treatDTOs.isEmpty())
            return List.of();

        return treatDTOs.stream()
                .filter(Objects::nonNull)
                .map(TreatMapper::toEntity)
                .collect(Collectors.toUnmodifiableList());
    }
}
