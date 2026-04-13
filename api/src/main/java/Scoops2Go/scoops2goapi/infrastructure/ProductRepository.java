package Scoops2Go.scoops2goapi.infrastructure;

import Scoops2Go.scoops2goapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.productType = :type")
    List<Product> findByProductType(@Param("type") String type);
}
