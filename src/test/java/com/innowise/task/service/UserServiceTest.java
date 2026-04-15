package com.innowise.task.service;

import com.innowise.task.dto.UserFilterDTO;
import com.innowise.task.dto.UserRequestDTO;
import com.innowise.task.dto.UserUpdateDTO;
import com.innowise.task.entity.UserStatus;
import com.innowise.task.repository.dao.UserRepository;
import com.innowise.task.entity.User;
import com.innowise.task.exceptions.NotFoundException;
import com.innowise.task.dto.UserDTO;
import com.innowise.task.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper usersMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private UserServiceImpl usersService;

    private UserRequestDTO requestDTO;
    private UserUpdateDTO updateDTO;
    private UserDTO userDTO;
    private User user;
    private final Long userId = 1L;

    @BeforeEach
    void setUp()
    {
        requestDTO = new UserRequestDTO();
        requestDTO.setName("John");
        requestDTO.setSurname("Doe");
        requestDTO.setEmail("john.doe@example.com");

        updateDTO = new UserUpdateDTO();
        updateDTO.setName("Nikita");
        updateDTO.setSurname("Shishko");
        updateDTO.setEmail("pokemon@gmail.com");

        userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setName("John");
        userDTO.setSurname("Doe");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setActive(UserStatus.ACTIVE);

        user = new User();
        user.setId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@example.com");
        user.setActive(UserStatus.ACTIVE);
    }

    @Test
    void create_shouldSaveUser_whenValid()
    {
        when(usersMapper.toEntityFromCreateRequest(requestDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(usersMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = usersService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("John");
        verify(usersMapper, times(1)).toEntityFromCreateRequest(requestDTO);
        verify(userRepository, times(1)).save(any(User.class));
        verify(usersMapper, times(1)).toDTO(user);
    }

    @Test
    void create_shouldThrow_whenDtoNull()
    {
        assertThatThrownBy(() -> usersService.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getById_shouldReturnUser_whenExists()
    {
        when(userRepository.getUsersById(userId)).thenReturn(Optional.of(user));
        when(usersMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = usersService.getById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository, times(1)).getUsersById(userId);
        verify(usersMapper, times(1)).toDTO(user);
    }

    @Test
    void getById_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> usersService.getById(null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldThrow_whenUserNotFound()
    {
        when(userRepository.getUsersById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.getById(userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_shouldReturnPageOfDTOUsers()
    {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setPageNumber(0);
        filter.setPageSize(10);
        filter.setSortedBy("id");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(usersMapper.toDTO(user)).thenReturn(userDTO);

        Page<UserDTO> result = usersService.getAll(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(userId);
        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(usersMapper, times(1)).toDTO(user);
    }

    @Test
    void updateById_shouldUpdateAndReturnUser()
    {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(usersMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = usersService.updateById(userId, updateDTO);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("John");
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verify(usersMapper, times(1)).toDTO(user);
    }

    @Test
    void updateById_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> usersService.updateById(null, updateDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateById_shouldThrow_whenUpdateDataNull()
    {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> usersService.updateById(userId, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateById_shouldThrow_whenUserNotFound()
    {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.updateById(userId, updateDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldDeleteUser_whenExists()
    {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        usersService.delete(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void delete_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> usersService.delete(null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenUserNotFound()
    {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> usersService.delete(userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldUpdateStatus()
    {
        when(userRepository.setActiveStatusOfUsers(userId, UserStatus.SLEEPY)).thenReturn(1);
        when(cacheManager.getCache("users")).thenReturn(cache);
        doNothing().when(cache).evict(userId);

        usersService.setActiveStatus(userId, UserStatus.SLEEPY);

        verify(userRepository, times(1)).setActiveStatusOfUsers(userId, UserStatus.SLEEPY);
        verify(cacheManager, times(1)).getCache("users");
        verify(cache, times(1)).evict(userId);
    }

    @Test
    void setActiveStatus_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> usersService.setActiveStatus(null, UserStatus.ACTIVE))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldThrow_whenStatusNull()
    {
        when(userRepository.setActiveStatusOfUsers(userId, null)).thenReturn(0);
        assertThatThrownBy(() -> usersService.setActiveStatus(userId, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldThrow_whenNotUpdated()
    {
        when(userRepository.setActiveStatusOfUsers(anyLong(), any(UserStatus.class))).thenReturn(0);

        assertThatThrownBy(() -> usersService.setActiveStatus(userId, UserStatus.HIDDEN))
                .isInstanceOf(NotFoundException.class);
    }
}