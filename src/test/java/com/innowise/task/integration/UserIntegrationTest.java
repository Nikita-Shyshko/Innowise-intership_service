package com.innowise.task.integration;

import com.innowise.task.dto.UserRequestDTO;
import com.innowise.task.dto.UserUpdateDTO;
import com.innowise.task.entity.UserStatus;
import com.innowise.task.repository.dao.UserRepository;
import com.innowise.task.entity.User;
import com.innowise.task.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserIntegrationTest extends BaseIntegrationTest
{
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup()
    {
        initUrls("/api/users", null);
    }

    private UserDTO createTestUser()
    {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("John");
        request.setSurname("Doe");
        request.setEmail("john.doe." + System.currentTimeMillis() + "@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(userBaseUrl, request, UserDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void createUser_shouldReturnCreatedUser()
    {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("John");
        request.setSurname("Doe");
        request.setEmail("john.doe@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(userBaseUrl, request, UserDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserDTO createdUser = response.getBody();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("John");
        assertThat(createdUser.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void getUserById_shouldReturnUser()
    {
        UserDTO createdUser = createTestUser();

        ResponseEntity<UserDTO> getResponse = restTemplate.getForEntity(
                userBaseUrl + "/" + createdUser.getId(), UserDTO.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("John");
    }

    @Test
    void getUserById_shouldBeCachedInRedis()
    {
        UserDTO createdUser = createTestUser();
        String cachedKey = "users::" + createdUser.getId();
        assertThat(redisTemplate.hasKey(cachedKey)).isTrue();
    }

    @Test
    void updateUserStatus_shouldChangeActiveStatus()
    {
        UserDTO createdUser = createTestUser();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<UserDTO> patchStatus = restTemplate.exchange(
                userBaseUrl + "/" + createdUser.getId() + "/status?status=INACTIVE",
                HttpMethod.PATCH, entity, UserDTO.class);
        assertThat(patchStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchStatus.getBody().getActive()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void updateUserDetails_shouldChangeNameAndEmail()
    {
        UserDTO createdUser = createTestUser();

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setName("Jane");
        updateDTO.setSurname("Doe");
        updateDTO.setEmail("jane.doe@example.com");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserUpdateDTO> updateEntity = new HttpEntity<>(updateDTO, headers);
        ResponseEntity<UserDTO> updateResponse = restTemplate.exchange(
                userBaseUrl + "/" + createdUser.getId(),
                HttpMethod.PATCH, updateEntity, UserDTO.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getName()).isEqualTo("Jane");
        assertThat(updateResponse.getBody().getEmail()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void deleteUser_shouldRemoveUserFromDatabase()
    {
        UserDTO createdUser = createTestUser();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                userBaseUrl + "/" + createdUser.getId(),
                HttpMethod.DELETE, entity, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<User> userFromDb = userRepository.findById(createdUser.getId());
        assertThat(userFromDb).isEmpty();
    }

    @Test
    void deleteUser_shouldMakeGetRequestReturnNotFound()
    {
        UserDTO createdUser = createTestUser();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        restTemplate.exchange(userBaseUrl + "/" + createdUser.getId(), HttpMethod.DELETE, entity, Void.class);

        ResponseEntity<String> deletedGet = restTemplate.getForEntity(
                userBaseUrl + "/" + createdUser.getId(), String.class);
        assertThat(deletedGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deletedGet.getBody()).contains("User not found");
    }
}