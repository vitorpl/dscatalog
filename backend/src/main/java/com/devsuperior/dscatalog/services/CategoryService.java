package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true) // ** importar do spring / readOnly melhora a performance não dando lock no bd
	public List<CategoryDTO> findAll() {
		List<Category> list = categoryRepository.findAll();
		List<CategoryDTO> dtos = 
				list.stream().map(cat -> new CategoryDTO(cat))
					.collect(Collectors.toList());
		return dtos;
	}

	
	public CategoryDTO findById(Long id) {
		Optional<Category> catOpt = categoryRepository.findById(id);
		return new CategoryDTO(
					catOpt.orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"))
				);
	}


	@Transactional
	public CategoryDTO salvar(CategoryDTO dto) {
		Category cat = new Category();
		cat.setName(dto.getName());
		cat = categoryRepository.save(cat);
		
		CategoryDTO dtoSalvo = new CategoryDTO(cat);
		
		return dtoSalvo;
	}
}
