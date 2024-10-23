package ma.alten.ecommerce.controller;


import ma.alten.ecommerce.model.Product;
import ma.alten.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // POST /products
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            // Adjust the inventory status based on the quantity
            if (product.getQuantity() > 10) {
                product.setInventoryStatus(Product.InventoryStatus.INSTOCK);
            } else if (product.getQuantity() > 0) {
                product.setInventoryStatus(Product.InventoryStatus.LOWSTOCK);
            } else if (product.getQuantity() == 0) {
                product.setInventoryStatus(Product.InventoryStatus.OUTOFSTOCK);
            }else {
                product.setQuantity(0);
                product.setInventoryStatus(Product.InventoryStatus.OUTOFSTOCK);
            }

            // Save the product with the updated inventory status
            Product savedProduct = productRepository.save(product);

            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // GET /products
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // GET /products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> updateProduct(@PathVariable("id") long id, @RequestBody Product product) {
        Optional<Product> existingProduct = productRepository.findById(id);

        if (existingProduct.isPresent()) {
            Product productToUpdate = existingProduct.get();

            // Update the product details
            productToUpdate.setName(product.getName());
            productToUpdate.setDescription(product.getDescription());
            productToUpdate.setPrice(product.getPrice());
            productToUpdate.setQuantity(product.getQuantity());
            productToUpdate.setImage(productToUpdate.getImage());
            // Adjust the inventory status based on the quantity
            if (productToUpdate.getQuantity() > 10) {
                product.setInventoryStatus(Product.InventoryStatus.INSTOCK);
            } else if (productToUpdate.getQuantity() > 0 && productToUpdate.getQuantity() <= 10) {
                product.setInventoryStatus(Product.InventoryStatus.LOWSTOCK);
            } else if (productToUpdate.getQuantity() == 0) {
                product.setInventoryStatus(Product.InventoryStatus.OUTOFSTOCK);
            }

            // Save the updated product
            Product updatedProduct = productRepository.save(productToUpdate);

            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DELETE /products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}