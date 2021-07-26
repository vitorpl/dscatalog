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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.RoleDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.Role;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.RoleRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class UserService {

	@Autowired
	private UserRepository repository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Transactional(readOnly = true) // ** importar do spring / readOnly melhora a performance não dando lock no bd
	public List<UserDTO> findAll() {
		List<User> list = repository.findAll();
		List<UserDTO> dtos = 
				list.stream().map(cat -> new UserDTO(cat))
					.collect(Collectors.toList());
		return dtos;
	}
	
	public Page<UserDTO> findAllPaged(Pageable pageRequest) {
		
		Page<User> list = repository.findAll(pageRequest);
		
		Page<UserDTO> dtos = 
				list.map(cat -> new UserDTO(cat));
		
		return dtos;
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> prodOpt = repository.findById(id);
		User entity = prodOpt.orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
		
		return new UserDTO(entity);
	}
	
	@Transactional
	public UserDTO insert(UserInsertDTO dto) {
		User entity = new User();
		dtoToEntity(dto, entity);
		
		entity.setPassword(
				passwordEncoder.encode(dto.getPassword()) );
		
		entity = repository.save(entity);
		
		UserDTO dtoSalvo = new UserDTO(entity);
		
		return dtoSalvo;
	}


	@Transactional
	public UserDTO update(Long id, UserUpdateDTO dto) {
		try {
			User prod = repository.getById(id);
			dtoToEntity(dto, prod);
			prod = repository.save(prod);
			return new UserDTO(prod);
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

	private void dtoToEntity(UserDTO dto, User entity) {
		//BeanUtils.copyProperties(dto, entity);
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());
		
		
		entity.getRoles().clear();
		for(RoleDTO roleDto : dto.getRoles() ) {
			Role role = roleRepository.getById(roleDto.getId()); 
			entity.getRoles().add(role);
		}
		
	}
}
