package com.innowise.task.service;

import com.innowise.task.entity.UserStatus;
import com.innowise.task.repository.dao.UserRepository;
import com.innowise.task.entity.User;
import com.innowise.task.exceptions.NotFoundException;
import com.innowise.task.util.ExceptionMessages;
import com.innowise.task.dto.UserDTO;
import com.innowise.task.mapper.UserMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl
{
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final UserMapper usersMapper;

    @CachePut(value = "users", key = "#result.id")
    public UserDTO create(@NotNull(message = ExceptionMessages.USER_DTO_MUST_NOT_BE_NULL) @Valid UserDTO userDto)
    {
        User users = usersMapper.toEntity(userDto);
        User savedUser = userRepository.save(users);

        log.info("The user has been created with ID: {}", savedUser.getId());
        return usersMapper.toDTO(savedUser);
    }

    @Cacheable(value = "users", key = "#id")
    public UserDTO getById(@NotNull(message = ExceptionMessages.USER_ID_MUST_NOT_BE_NULL) Long id)
    {
        log.debug("Fetching user by id: {}", id);
        return userRepository.getUsersById(id).map(usersMapper::toDTO)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.USER_NOT_FOUND + id));
    }

    public Page<UserDTO> getAll(Specification<User> specification, Pageable pageable)
    {
        Specification<User> specific = (Specification<User>) specification;

        log.debug("Fetching all users");

        return userRepository.findAll(specific, pageable).map(usersMapper::toDTO);
    }

    @CachePut(value = "users", key = "#id")
    public UserDTO updateById(
            @NotNull(message = ExceptionMessages.USER_ID_MUST_NOT_BE_NULL) Long id,
            String name,
            String surname,
            String email)
    {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.USER_NOT_FOUND + id));

        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);

        User updatedUser = userRepository.save(user);
        log.info("User with id={} has been updated", id);
        return usersMapper.toDTO(updatedUser);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "cardsByUserId", key = "#id")
    })

    public void delete(@NotNull(message = ExceptionMessages.USER_ID_MUST_NOT_BE_NULL) Long id)
    {
        if(!userRepository.existsById(id))
        {
            throw new NotFoundException(ExceptionMessages.USER_NOT_FOUND + id);
        }

        log.info("The user with ID: {} has been successfully removed", id);
        userRepository.deleteById(id);
    }

    public void setActiveStatus(
            @NotNull(message = ExceptionMessages.USER_ID_MUST_NOT_BE_NULL) Long id,
            @NotNull(message = "User status must not be null") UserStatus userStatus)
    {
        int setStatus = userRepository.setActiveStatusOfUsers(id, userStatus);
        if (setStatus == 0)
        {
            throw new NotFoundException(ExceptionMessages.USER_NOT_UPDATED + id);
        }

        Cache cache = cacheManager.getCache("users");
        if (cache != null)
        {
            cache.evict(id);
        }
        log.info("The user with ID: {} status has been successfully changed", id);
    }
}
