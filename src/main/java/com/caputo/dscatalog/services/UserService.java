package com.caputo.dscatalog.services;

import com.caputo.dscatalog.Repositories.RoleRepository;
import com.caputo.dscatalog.Repositories.UserRepository;
import com.caputo.dscatalog.dto.RoleDTO;
import com.caputo.dscatalog.dto.UserDTO;
import com.caputo.dscatalog.dto.UserInsertDTO;
import com.caputo.dscatalog.dto.UserUpdateDTO;
import com.caputo.dscatalog.entities.Role;
import com.caputo.dscatalog.entities.User;
import com.caputo.dscatalog.services.exceptions.DatabaseException;
import com.caputo.dscatalog.services.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class UserService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
    public UserDTO insert(UserInsertDTO dto){
        User entity = new User();
        copyDtoToEntity(dto, entity);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = userRep.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto){
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       User user = userRep.findByEmail(username);
       if (user == null){
           logger.error("User not found: " + username);
           throw new UsernameNotFoundException("Email not found!");
       }
        logger.info("User found: " + username);
       return user;
    }
}
