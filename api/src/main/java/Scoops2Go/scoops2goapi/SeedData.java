package Scoops2Go.scoops2goapi;

import Scoops2Go.scoops2goapi.model.Cone;
import Scoops2Go.scoops2goapi.model.Flavor;
import Scoops2Go.scoops2goapi.model.Topping;

import java.math.BigDecimal;
import java.util.Arrays;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepo) {
        return args -> {

            productRepo.save(new Cone("Waffle Cone", BigDecimal.valueOf(2.00), "A classic crispy waffle cone, perfect for any scoop.", Arrays.asList("Wheat flour", "sugar", "vegetable oil", "eggs", "salt")));
            productRepo.save(new Cone("Sugar Cone", BigDecimal.valueOf(1.50), "A sweet, crunchy cone with a light flavor.", Arrays.asList("Wheat flour", "sugar", "corn syrup", "vegetable oil", "salt")));
            productRepo.save(new Cone("Cup", BigDecimal.valueOf(1.00), "A simple cup for those who prefer no cone.", Arrays.asList("Paper", "food-safe coating (beeswax, eggs, coconut oil)")));

            productRepo.save(new Flavor("Vanilla", BigDecimal.valueOf(1.00), "Smooth and creamy classic vanilla ice cream.", Arrays.asList("Milk", "cream", "sugar", "vanilla extract")));
            productRepo.save(new Flavor("Chocolate", BigDecimal.valueOf(1.00), "Rich and indulgent chocolate ice cream.", Arrays.asList("Milk", "cream", "sugar", "cocoa powder")));
            productRepo.save(new Flavor("Strawberry", BigDecimal.valueOf(1.00), "Sweet and fruity strawberry ice cream.", Arrays.asList("Milk", "cream", "sugar", "strawberries")));
            productRepo.save(new Flavor("Mint Chocolate Chip", BigDecimal.valueOf(1.20), "Refreshing mint ice cream with chocolate chips.", Arrays.asList("Milk", "cream", "sugar", "mint extract", "chocolate chips")));
            productRepo.save(new Flavor("Salted Caramel", BigDecimal.valueOf(1.25), "Creamy caramel with a hint of sea salt.", Arrays.asList("Milk", "cream", "sugar", "caramel", "sea salt")));
            productRepo.save(new Flavor("Cookies & Cream", BigDecimal.valueOf(1.25), "Classic cookies folded into creamy ice cream.", Arrays.asList("Milk", "cream", "sugar", "chocolate sandwich cookies")));
            productRepo.save(new Flavor("Pistachio", BigDecimal.valueOf(1.30), "Nutty and smooth pistachio ice cream.", Arrays.asList("Milk", "cream", "sugar", "pistachios (contains nuts)")));
            productRepo.save(new Flavor("Lemon Sorbet", BigDecimal.valueOf(1.10), "Light and zesty dairy-free lemon sorbet.", Arrays.asList("Water", "sugar", "lemon juice", "lemon zest")));

            productRepo.save(new Topping("Sprinkles", BigDecimal.valueOf(0.50), "Colorful candy sprinkles for a fun finish.", Arrays.asList("Sugar", "corn starch", "food coloring")));
            productRepo.save(new Topping("Chocolate Chips", BigDecimal.valueOf(0.75), "Crunchy chocolate chips for extra delight.", Arrays.asList("Sugar", "cocoa butter", "cocoa mass", "milk powder")));
            productRepo.save(new Topping("Caramel Sauce", BigDecimal.valueOf(0.80), "Smooth and sweet caramel sauce drizzle.", Arrays.asList("Sugar", "cream", "butter")));
            productRepo.save(new Topping("Hot Fudge", BigDecimal.valueOf(0.85), "Warm chocolate fudge sauce.", Arrays.asList("Sugar", "cocoa", "cream", "butter")));
            productRepo.save(new Topping("Whipped Cream", BigDecimal.valueOf(0.60), "Light and fluffy whipped cream.", Arrays.asList("Cream", "sugar")));
            productRepo.save(new Topping("Chopped Nuts", BigDecimal.valueOf(0.70), "A crunchy mix of chopped nuts.", Arrays.asList("Nuts", "cinnamon")));
            productRepo.save(new Topping("Fresh Strawberries", BigDecimal.valueOf(0.90), "Sliced fresh strawberries.", Arrays.asList("Strawberries", "sugar")));
        };
    }
}
