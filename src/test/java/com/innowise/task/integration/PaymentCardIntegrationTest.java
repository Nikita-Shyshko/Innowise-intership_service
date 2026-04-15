package com.innowise.task.integration;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.dto.PaymentCardRequestDTO;
import com.innowise.task.dto.UserDTO;
import com.innowise.task.dto.UserRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentCardIntegrationTest extends BaseIntegrationTest
{
    @BeforeEach
    void setup()
    {
        initUrls("/api/users", "/api/cards");
    }

    private Long createTestUser()
    {
        UserRequestDTO userRequest = new UserRequestDTO();
        userRequest.setName("Card");
        userRequest.setSurname("Holder");
        String uniqueEmail = "card.holder." + System.currentTimeMillis() + "@example.com";
        userRequest.setEmail(uniqueEmail);
        userRequest.setBirthDate(LocalDate.of(1995, 5, 15));
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(userBaseUrl, userRequest, UserDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().getId();
    }

    private PaymentCardDTO createTestCard(Long userId)
    {
        PaymentCardRequestDTO cardRequest = new PaymentCardRequestDTO();
        cardRequest.setUserId(userId);
        String uniqueNumber = "12345678" + (System.currentTimeMillis() % 100000000);
        cardRequest.setNumber(uniqueNumber);
        cardRequest.setHolder("JOHN DOE");
        cardRequest.setExpirationDate(LocalDate.of(2030, 12, 31));
        ResponseEntity<PaymentCardDTO> response = restTemplate.postForEntity(cardBaseUrl, cardRequest, PaymentCardDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void createPaymentCard_shouldReturnCreatedCard()
    {
        Long userId = createTestUser();
        PaymentCardDTO createdCard = createTestCard(userId);
        assertThat(createdCard).isNotNull();
        assertThat(createdCard.getId()).isNotNull();
        assertThat(createdCard.getNumber()).isNotNull();
        assertThat(createdCard.getNumber()).startsWith("12345678");
    }

    @Test
    void getPaymentCardById_shouldReturnCard()
    {
        Long userId = createTestUser();
        PaymentCardDTO createdCard = createTestCard(userId);

        ResponseEntity<PaymentCardDTO> getResponse = restTemplate.getForEntity(
                cardBaseUrl + "/" + createdCard.getId(), PaymentCardDTO.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getHolder()).isEqualTo("JOHN DOE");
    }

    @Test
    void deactivatePaymentCard_shouldUpdateStatusToFalse()
    {
        Long userId = createTestUser();
        PaymentCardDTO createdCard = createTestCard(userId);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<PaymentCardDTO> patchResponse = restTemplate.exchange(
                cardBaseUrl + "/" + createdCard.getId() + "?status=false",
                HttpMethod.PATCH, entity, PaymentCardDTO.class);
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchResponse.getBody().getActive()).isFalse();
    }

    @Test
    void updatePaymentCard_shouldUpdateFields()
    {
        Long userId = createTestUser();
        PaymentCardDTO createdCard = createTestCard(userId);

        PaymentCardDTO updateDto = new PaymentCardDTO();
        updateDto.setId(createdCard.getId());
        updateDto.setUserId(userId);
        String newNumber = "87654321" + (System.currentTimeMillis() % 100000000);
        updateDto.setNumber(newNumber);
        updateDto.setHolder("JANE DOE");
        updateDto.setExpirationDate(LocalDate.of(2035, 1, 1));
        updateDto.setActive(true);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<PaymentCardDTO> updateEntity = new HttpEntity<>(updateDto, headers);
        ResponseEntity<PaymentCardDTO> updateResponse = restTemplate.exchange(
                cardBaseUrl + "/" + createdCard.getId(),
                HttpMethod.PUT, updateEntity, PaymentCardDTO.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getHolder()).isEqualTo("JANE DOE");
    }

    @Test
    void getUserCards_shouldReturnListOfCards()
    {
        Long userId = createTestUser();
        PaymentCardDTO createdCard = createTestCard(userId);

        ResponseEntity<List<PaymentCardDTO>> cardsResponse = restTemplate.exchange(
                cardBaseUrl + "/user/" + userId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PaymentCardDTO>>() {}
        );
        assertThat(cardsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cardsResponse.getBody()).hasSize(1);
        assertThat(cardsResponse.getBody().get(0).getId()).isEqualTo(createdCard.getId());
    }

    @Test
    void deletePaymentCard_shouldRemoveCard()
    {
        Long userId = createTestUser();
        PaymentCardDTO createdCard = createTestCard(userId);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                cardBaseUrl + "/" + createdCard.getId(),
                HttpMethod.DELETE, entity, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getDeleted = restTemplate.getForEntity(
                cardBaseUrl + "/" + createdCard.getId(), String.class);
        assertThat(getDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}