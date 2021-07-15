package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.tests.TestFactory;

@DataJpaTest
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repository;
	
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 99999999L;
		countTotalProducts = 25L;
	}
	
	@Test
	public void saveShouldPersistWithAutoincrementWhenNullId() {
		Product product = TestFactory.createProduct();
		product.setId(null);
		
		product = repository.save(product);
		
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenExists() {
		//Arrange
		//long existingId = 1L;
		
		//Act
		repository.deleteById(1L);
		
		//Assert
		Optional<Product> result = repository.findById(existingId);
		//Objeto não deve estar presente
		Assertions.assertFalse(result.isPresent());		
	}
	
	@Test
	public void deleteShouldThrowExceptionWhenIdDoesNotExists() {
		
		//long nonExistingId = 999999L;
		
		Assertions.assertThrows(EmptyResultDataAccessException.class, 
				() -> {
					repository.deleteById(nonExistingId);
				});
		
	}
	
	@Test
	public void findByIdShouldReturnEmptyWhenNonExistingId() {
		//Assertions.fail("Not implemented");
		
		Optional<Product> productOpt = repository.findById(existingId);

		Assertions.assertTrue(productOpt.isPresent());		
	}
	
	@Test
	public void findByIdShouldReturnProductWhenExistingId() {
		//Assertions.fail("Not implemented");
		Optional<Product> productOpt = repository.findById(nonExistingId);
		
		Assertions.assertFalse(productOpt.isPresent());
	}
}
