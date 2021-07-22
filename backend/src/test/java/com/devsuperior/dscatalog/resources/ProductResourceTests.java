package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.TestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	//@Mock mais utilizado no service / não carrega o contexto da aplicação
	//https://docs.google.com/document/d/1GqPMgsXmKA5QzzBUsK09I2ierXjGSMNxXAXMFAHmEJk/edit
	//https://stackoverflow.com/questions/44200720/difference-between-mock-mockbean-and-mockito-mock
	@MockBean //usado quando a classe carrega o contexto da aplicação e precisa mokar algum bean do sistema
	private ProductService service;
	
	private ProductDTO  productDTO;
	private PageImpl<ProductDTO> page; //precisa de um objeto concreto para poder dar new
	private Long existingId = 1L;
	private Long nonExistingId = 2L;
	private Long dependentId = 3L;
	
	@BeforeEach
	void setUp() throws Exception {
		
		productDTO = TestFactory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));
		
		//import static org.mockito.ArgumentMatchers.any;
		//any() pois o argumento não importa muito para o teste sendo executado
		when(service.findAllPaged(any())).thenReturn(page);
		
		when(service.findById(existingId)).thenReturn(productDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		//eq evita erro ao usar o any
		when(service.atualizar(eq(existingId), any())).thenReturn(productDTO);
		when(service.atualizar(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
		
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DatabaseException.class).when(service).delete(dependentId);
		
		when(service.salvar(any())).thenReturn(productDTO);
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception {
		//perform faz uma requisição
		//mockMvc.perform(get("/products")).andExpect(status().isOk());
		//.andExpect(status().isOk()) é a Assertion
		
		//Para legibilidade, pode-se dividir os métodos chamados no mockMvc como demonstrado:
		ResultActions resultAct = 
				mockMvc.perform(get("/products")
						.accept(MediaType.APPLICATION_JSON)); //tratamento da mídia de retorno
		
		resultAct.andExpect(status().isOk());		
	}
	
	@Test
	public void findByIdShouldReturnProductDTOWhenExistingId() throws Exception {
		ResultActions resultAct = 
				mockMvc.perform(get("/products/{id}", existingId)
						.accept(MediaType.APPLICATION_JSON)); 
		resultAct.andExpect(status().isOk());
		//jsonPath analisa o corpo da resposta
		resultAct.andExpect(jsonPath("$.id").exists()); //no corpo da resposta $ deve existir campo id
		resultAct.andExpect(jsonPath("$.name").exists());
		resultAct.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void findByIdShouldReturnStatusFoundWhenIdDoesNotExists() throws Exception {
		ResultActions resultAct = 
				mockMvc.perform(get("/products/{id}", nonExistingId)
						.accept(MediaType.APPLICATION_JSON)); 
		resultAct.andExpect(status().isNotFound());
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenExistingId() throws Exception {
		//put/post tem um body então preparar a requisição:
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions resultAct = 
				mockMvc.perform(put("/products/{id}", existingId)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		resultAct.andExpect(status().isOk());
		resultAct.andExpect(jsonPath("$.id").exists());
		resultAct.andExpect(jsonPath("$.name").exists());
		resultAct.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions resultAct = 
				mockMvc.perform(put("/products/{id}", nonExistingId)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		resultAct.andExpect(status().isNotFound());
	}
	
	@Test
	public void insertShouldReturnProductDTOAndCreatedStatus() throws Exception  {
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		ResultActions resultAct = 
				mockMvc.perform(post("/products")
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		resultAct.andExpect(status().isCreated());
		
		resultAct.andExpect(jsonPath("$.id").exists());
		resultAct.andExpect(jsonPath("$.name").exists());
		resultAct.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenExistingId() throws Exception {
		ResultActions resultAct = 
				mockMvc.perform(delete("/products/{id}", existingId));
		resultAct.andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteShouldRetunrNotFoundWhenIdDoesNotExist() throws Exception {
		ResultActions resultAct = 
				mockMvc.perform(delete("/products/{id}", nonExistingId));
		resultAct.andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteShouldReturnBadRequestWhenDependentId() throws Exception {
		ResultActions resultAct = 
				mockMvc.perform(delete("/products/{id}", dependentId));
		resultAct.andExpect(status().isBadRequest());
	}
}
