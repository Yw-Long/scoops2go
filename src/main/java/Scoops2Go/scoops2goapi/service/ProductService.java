package Scoops2Go.scoops2goapi.service;

import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.exception.ResourceNotFoundException;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import Scoops2Go.scoops2goapi.mapper.OrderMapper;
import Scoops2Go.scoops2goapi.mapper.ProductMapper;
import Scoops2Go.scoops2goapi.model.Order;
import Scoops2Go.scoops2goapi.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductDTO getProductById(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        return ProductMapper.toDto(product);
    }

    public List<ProductDTO> getProducts() {
        List<Product> products = productRepository.findAll();
        return ProductMapper.toDtoList(products);
    }
}
