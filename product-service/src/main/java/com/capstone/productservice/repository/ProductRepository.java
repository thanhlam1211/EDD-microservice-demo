package com.capstone.productservice.repository;

import com.capstone.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface ProductRepository extends MongoRepository<Product,String> {
}
