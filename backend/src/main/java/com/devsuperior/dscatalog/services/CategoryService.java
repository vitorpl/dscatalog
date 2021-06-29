package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository repository;
	
	@Transactional(readOnly = true) // ** importar do spring / readOnly melhora a performance não dando lock no bd
	public List<CategoryDTO> findAll() {
		List<Category> list = repository.findAll();
		List<CategoryDTO> dtos = 
				list.stream().map(cat -> new CategoryDTO(cat))
					.collect(Collectors.toList());
		return dtos;
	}
	
	public Page<CategoryDTO> findAllPaged(Pageable pageable) {
		
		Page<Category> list = repository.findAll(pageable);
		
		Page<CategoryDTO> dtos = 
				list.map(cat -> new CategoryDTO(cat));
		
		return dtos;
	}
	
	public CategoryDTO findById(Long id) {
		Optional<Category> catOpt = repository.findById(id);
		return new CategoryDTO(
					catOpt.orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"))
				);
	}

	@Transactional
	public CategoryDTO salvar(CategoryDTO dto) {
		Category cat = new Category();
		cat.setName(dto.getName());
		cat = repository.save(cat);
		
		CategoryDTO dtoSalvo = new CategoryDTO(cat);
		
		return dtoSalvo;
	}

	@Transactional
	public CategoryDTO atualizar(Long id, CategoryDTO dto) {
		
		try {
			Category category = repository.getById(id);
			category.setName(dto.getName());
			category = repository.save(category);
			return new CategoryDTO(category);
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

}
