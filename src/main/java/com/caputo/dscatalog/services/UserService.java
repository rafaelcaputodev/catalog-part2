package com.caputo.dscatalog.services;

import com.caputo.dscatalog.Repositories.RoleRepository;
import com.caputo.dscatalog.Repositories.UserRepository;
import com.caputo.dscatalog.dto.RoleDTO;
import com.caputo.dscatalog.dto.UserDTO;
import com.caputo.dscatalog.entities.Role;
import com.caputo.dscatalog.entities.User;
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
public class UserService {

    @Autowired
    private UserRepository userRep;

    @Autowired
    private RoleRepository roleRep;

    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(PageRequest request){
        Page<User> page = userRep.findAll(request);
        return page.map(x -> new UserDTO(x));
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id){
        User user = userRep.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO insert(UserDTO dto){
        User entity = new User();
        copyDtoToEntity(dto, entity);
        entity = userRep.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserDTO dto){
        try {
            User entity = userRep.getOne(id);
            copyDtoToEntity(dto, entity);
            entity = userRep.save(entity);
            return new UserDTO(entity);
        } catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not found" + id);
        }
    }

    public void delete (Long id){
       try {
           userRep.deleteById(id);
       } catch (EmptyResultDataAccessException e){
           throw new ResourceNotFoundException("Id not found" + id);
       } catch (DataIntegrityViolationException e){
           throw new DatabaseException("Integrity violation");
       }
    }

    private void copyDtoToEntity(UserDTO dto, User entity){
        entity.setEmail(dto.getEmail());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());

        entity.getRoles().clear();
        for(RoleDTO roleDTO : dto.getRoles()){
            Role role = roleRep.getOne(roleDTO.getId());
            entity.getRoles().add(role);
        }
    }
}
