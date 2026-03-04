package com.innowise.task.repository;

import com.innowise.task.entity.Users;
import com.innowise.task.dto.UserDTO;
import com.innowise.task.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UsersRepositoryImpl
{
    UserDTO create(UserDTO dto);
    UserDTO getById(Long id);
    Page<UserDTO> getAll(Specification<Users> specification, Pageable pageable);
    void delete(Long id);
    UserDTO updateById(Long id, String name, String surname, String email);
    void setActiveStatus(Long id, UserStatus userStatus);
}