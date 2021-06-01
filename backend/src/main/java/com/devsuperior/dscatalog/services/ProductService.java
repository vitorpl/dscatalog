package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true) // ** importar do spring / readOnly melhora a performance não dando lock no bd
	public List<ProductDTO> findAll() {
		List<Product> list = repository.findAll();
		List<ProductDTO> dtos = 
				list.stream().map(cat -> new ProductDTO(cat))
					.collect(Collectors.toList());
		return dtos;
	}
	
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		
		Page<Product> list = repository.findAll(pageRequest);
		
		Page<ProductDTO> dtos = 
				list.map(cat -> new ProductDTO(cat));
		
		return dtos;
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> prodOpt = repository.findById(id);
		Product entity = prodOpt.orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
		System.out.println(entity.getCategories().size());
		return new ProductDTO(
					entity,				
					entity.getCategories()
				);
	}
	
	@Transactional
	public ProductDTO salvar(ProductDTO dto) {
		Product prod = new Product();
		dtoToEntity(dto, prod);
		prod = repository.save(prod);
		
		ProductDTO dtoSalvo = new ProductDTO(prod);
		
		return dtoSalvo;
	}


	@Transactional
	public ProductDTO atualizar(Long id, ProductDTO dto) {
		try {
			Product prod = repository.getById(id);
			dtoToEntity(dto, prod);
			prod = repository.save(prod);
			return new ProductDTO(prod);
		}
		catch(EntityNotFoundException ex) {
			throw new ResourceNotFoundException("Id not found "+ id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		}
		catch(EmptyResultDataAccessException ex) {
			throw new ResourceNotFoundException("Id not found "+ id);
		}
		catch(DataIntegrityViolationException dataEx) {
			throw new DatabaseException("Recurso não pode ser excluído pois possui registros dependentes");
		}
	}

	private void dtoToEntity(ProductDTO dto, Product entity) {
		//BeanUtils.copyProperties(dto, entity);
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPrice(dto.getPrice());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		
		entity.getCategories().clear();
		for(CategoryDTO catDto : dto.getCategories() ) {
			Category category = categoryRepository.getById(catDto.getId()); 
			entity.getCategories().add(category);
		}
		
	}
}
