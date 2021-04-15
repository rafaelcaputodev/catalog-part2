package com.caputo.dscatalog.services;


import com.caputo.dscatalog.Repositories.CategoryRepository;
import com.caputo.dscatalog.Repositories.ProductRepository;
import com.caputo.dscatalog.dto.CategoryDTO;
import com.caputo.dscatalog.dto.ProductDTO;
import com.caputo.dscatalog.entities.Category;
import com.caputo.dscatalog.entities.Product;
import com.caputo.dscatalog.services.exceptions.DatabaseException;
import com.caputo.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRep;

	@Autowired
	private CategoryRepository categoryRep;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> list = productRep.findAll(pageRequest);
		return list.map(x -> new ProductDTO(x));
	}
	
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Product entity = productRep.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
    public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = productRep.save(entity);
		return new ProductDTO(entity);
    }

	@Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = productRep.getOne(id);
			copyDtoToEntity(dto, entity);
			entity = productRep.save(entity);
			return new ProductDTO(entity);
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
    }

    public void delete(Long id) {
		try {
			productRep.deleteById(id);
		}
		catch (EmptyResultDataAccessException e){
			throw new ResourceNotFoundException("Id not found " + id);
		}
		catch (DataIntegrityViolationException e){
			throw new DatabaseException("Integrity violation");
		}
    }

	private void copyDtoToEntity(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());

		entity.getCategories().clear();
		for (CategoryDTO cat : dto.getCategories()){
			Category category = categoryRep.getOne(cat.getId());
			entity.getCategories().add(category);
		}
	}
}