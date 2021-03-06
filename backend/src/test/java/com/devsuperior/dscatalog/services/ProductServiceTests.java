package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.NoMoreInteractions;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.TestFactory;


/**
 * @ExtendWith(SpringExtension.class)
 * Não carrega o contexto, mas permite usar os recursos do
 * Spring com JUnit 
 * (Teste de unidade: service/component) *
 */
@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
	
	@InjectMocks
	private ProductService service;
	
	/** Para teste de unidade que não carrega o contexto */
	@Mock /* não quero testar o repository então eu crio um repository fake */
	private ProductRepository repository;
	/** Também é preciso configurar o comportamento do Mock */
	
	@Mock
	private CategoryRepository categoryRepository;
	
	/** Mockito do Srpring
	 * Para teste de unidade que precisa do contexto para mockar
	 * algum bean do sistema */
//	@MockBean
//	private ProductRepository repository2;
	
	private long existingId;
	private long nonExistingId;
	private long existingCatId;
	private long dependentId;
	private PageImpl page;
	private Product product;
	private Category testCategory;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		
		existingCatId = 1L;
		
		product = TestFactory.createProduct();
		page = new PageImpl<>(List.of(product));
		testCategory = TestFactory.createCategory();
		
		//configurando o comportamento do Mock
		//quando chamar o find all passando qualquer valor (ArgumentMatchers.any())
		Mockito.when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
		
		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
		
		//Mockito.doThrow(ResourceNotFoundException.class).when(repository).findById(nonExistingId);
		Mockito.when(repository.getById(existingId)).thenReturn(product);
		//Mockito.doThrow(ResourceNotFoundException.class).when(repository).getById(nonExistingId);
		Mockito.when(repository.getById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		//Config do CategoryRepo
		Mockito.when(categoryRepository.getById(existingCatId)).thenReturn(testCategory);
		Mockito.when(categoryRepository.getById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
	}
	
	@Test
	public void findByIdShouldReturnDTOWhenExistingId() {
		ProductDTO productDTO = service.findById(existingId);		
		Assertions.assertNotNull(productDTO);
	}
	
	@Test
	public void findByIdShouldThrowEntityNotFoundExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			service.findById(nonExistingId);		
		});
	}
	
	@Test
	public void updateShouldReturnProductWhenExistingId() {
		ProductDTO dto = TestFactory.createProductDTO();
		ProductDTO reusult = service.atualizar(existingId, dto);
		
		Mockito.verify(categoryRepository).getById(existingCatId);
		
		Assertions.assertNotNull(reusult);
	}
	
	@Test
	public void updataShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		ProductDTO dto = TestFactory.createProductDTO();
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.atualizar(nonExistingId, dto);
		});
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		//Mockito.verify(repository, Mockito.times(1)).findAll(pageable); mesmo que sem o times
		Mockito.verify(repository).findAll(pageable);
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		
		//Mockito pode verificar se foi chamado algum método do mock
		Mockito.verify(repository).deleteById(existingId);
		
		//deve ser chamado n vezes
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
		
		//nunca deve ser chamado
		//Mockito.verify(repository, Mockito.never()).deleteById(existingId);
	}
	
	@Test
	public void deleteShouldThrowExceptionWhenIdNotExists() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
	}
	
	
}
