package com.innowise.task.service;

import com.innowise.task.dto.PaymentCardFilterDTO;
import com.innowise.task.dto.PaymentCardRequestDTO;
import com.innowise.task.repository.dao.PaymentCardRepository;
import com.innowise.task.repository.dao.UserRepository;
import com.innowise.task.entity.PaymentCard;
import com.innowise.task.entity.User;
import com.innowise.task.exceptions.TooManyCardsForUserException;
import com.innowise.task.exceptions.NotFoundException;
import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.mapper.PaymentCardMapper;
import com.innowise.task.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardTest
{
    @Mock
    private PaymentCardRepository paymentCardsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardsService;

    private PaymentCardRequestDTO requestDTO;
    private PaymentCardDTO paymentCardDTO;
    private PaymentCard card;
    private User user;
    private final Long cardId = 1L;
    private final Long userId = 10L;

    @BeforeEach
    void setUp()
    {
        requestDTO = new PaymentCardRequestDTO();
        requestDTO.setUserId(userId);
        requestDTO.setNumber("1234567812345678");
        requestDTO.setHolder("Nikita Shishko");

        paymentCardDTO = new PaymentCardDTO();
        paymentCardDTO.setId(cardId);
        paymentCardDTO.setUserId(userId);
        paymentCardDTO.setNumber("1234567812345678");
        paymentCardDTO.setHolder("Nikita Shishko");
        paymentCardDTO.setActive(true);

        user = new User();
        user.setId(userId);

        card = new PaymentCard();
        card.setId(cardId);
        card.setUser(user);
        card.setNumber(paymentCardDTO.getNumber());
        card.setHolder(paymentCardDTO.getHolder());
        card.setActive(true);
    }

    @Test
    void create_shouldSaveCard_whenValid()
    {
        when(userRepository.getUsersById(userId)).thenReturn(Optional.of(user));
        when(paymentCardsRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(paymentCardMapper.toEntityFromCreateRequest(requestDTO)).thenReturn(card);
        when(paymentCardsRepository.save(any(PaymentCard.class))).thenReturn(card);
        when(paymentCardMapper.toDto(card)).thenReturn(paymentCardDTO);

        PaymentCardDTO result = paymentCardsService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(cardId);
        verify(paymentCardMapper, times(1)).toEntityFromCreateRequest(requestDTO);
        verify(paymentCardsRepository, times(1)).save(any(PaymentCard.class));
        verify(paymentCardMapper, times(1)).toDto(card);
    }

    @Test
    void create_shouldThrow_whenDtoNull()
    {
        assertThatThrownBy(() -> paymentCardsService.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void create_shouldThrow_whenUserNotFound()
    {
        when(userRepository.getUsersById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardsService.create(requestDTO))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Throws TooManyCardsForUserException when user already has 5 cards")
    void create_shouldThrow_whenUserHasFiveCards()
    {
        when(userRepository.getUsersById(userId)).thenReturn(Optional.of(user));
        List<PaymentCard> fiveCards = List.of(
                new PaymentCard(), new PaymentCard(), new PaymentCard(), new PaymentCard(), new PaymentCard()
        );
        when(paymentCardsRepository.findAllByUserId(userId)).thenReturn(fiveCards);

        assertThatThrownBy(() -> paymentCardsService.create(requestDTO))
                .isInstanceOf(TooManyCardsForUserException.class);
    }

    @Test
    void getById_shouldReturnCard_whenExists()
    {
        when(paymentCardsRepository.getPaymentCardsById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(paymentCardDTO);

        PaymentCardDTO result = paymentCardsService.getById(cardId);

        assertThat(result.getId()).isEqualTo(cardId);
        verify(paymentCardsRepository, times(1)).getPaymentCardsById(cardId);
        verify(paymentCardMapper, times(1)).toDto(card);
    }

    @Test
    void getById_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> paymentCardsService.getById(null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldThrow_whenCardNotFound()
    {
        when(paymentCardsRepository.getPaymentCardsById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardsService.getById(cardId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_shouldReturnPageOfDTO()
    {
        PaymentCardFilterDTO filter = new PaymentCardFilterDTO();
        filter.setPageNumber(0);
        filter.setPageSize(10);
        filter.setSortedBy("id");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card), pageable, 1);

        when(paymentCardsRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
        when(paymentCardMapper.toDto(card)).thenReturn(paymentCardDTO);

        Page<PaymentCardDTO> result = paymentCardsService.getAll(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(cardId);
        verify(paymentCardsRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(paymentCardMapper, times(1)).toDto(card);
    }

    @Test
    void updatePaymentCardsById_shouldUpdateAndReturnCard()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardsRepository.save(any(PaymentCard.class))).thenReturn(card);
        when(paymentCardMapper.toDto(card)).thenReturn(paymentCardDTO);

        PaymentCardDTO result = paymentCardsService.updatePaymentCardsById(cardId, paymentCardDTO);

        assertThat(result.getId()).isEqualTo(cardId);
        verify(paymentCardsRepository, times(1)).findById(cardId);
        verify(paymentCardsRepository, times(1)).save(card);
        verify(paymentCardMapper, times(1)).toDto(card);
    }

    @Test
    void updatePaymentCardsById_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> paymentCardsService.updatePaymentCardsById(null, paymentCardDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updatePaymentCardsById_shouldThrow_whenDtoNull()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThatThrownBy(() -> paymentCardsService.updatePaymentCardsById(cardId, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updatePaymentCardsById_shouldThrow_whenCardNotFound()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardsService.updatePaymentCardsById(cardId, paymentCardDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldDeleteCard_whenExists()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cacheManager.getCache("cards")).thenReturn(cache);
        when(cacheManager.getCache("cardsByUserId")).thenReturn(cache);
        doNothing().when(cache).evict(cardId);
        doNothing().when(cache).evict(userId);
        doNothing().when(paymentCardsRepository).delete(card);

        paymentCardsService.delete(cardId);

        verify(paymentCardsRepository, times(1)).findById(cardId);
        verify(paymentCardsRepository, times(1)).delete(card);
        verify(cacheManager, times(1)).getCache("cards");
        verify(cacheManager, times(1)).getCache("cardsByUserId");
        verify(cache, times(1)).evict(cardId);
        verify(cache, times(1)).evict(userId);
    }

    @Test
    void delete_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> paymentCardsService.delete(null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenCardNotFound()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardsService.delete(cardId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldUpdateStatus()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardsRepository.setActiveStatusOfPaymentCards(cardId, true)).thenReturn(1);
        when(cacheManager.getCache("cards")).thenReturn(cache);
        when(cacheManager.getCache("cardsByUserId")).thenReturn(cache);
        doNothing().when(cache).evict(cardId);
        doNothing().when(cache).evict(userId);

        paymentCardsService.setActiveStatus(cardId, true);

        verify(paymentCardsRepository, times(1)).findById(cardId);
        verify(paymentCardsRepository, times(1)).setActiveStatusOfPaymentCards(cardId, true);
        verify(cacheManager, times(1)).getCache("cards");
        verify(cacheManager, times(1)).getCache("cardsByUserId");
        verify(cache, times(1)).evict(cardId);
        verify(cache, times(1)).evict(userId);
    }

    @Test
    void setActiveStatus_shouldThrow_whenIdNull()
    {
        assertThatThrownBy(() -> paymentCardsService.setActiveStatus(null, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldThrow_whenCardNotFound()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardsService.setActiveStatus(cardId, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldThrow_whenNotUpdated()
    {
        when(paymentCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardsRepository.setActiveStatusOfPaymentCards(anyLong(), anyBoolean())).thenReturn(0);

        assertThatThrownBy(() -> paymentCardsService.setActiveStatus(cardId, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByUserId_shouldReturnCardsList()
    {
        when(paymentCardsRepository.findAllByUserId(userId)).thenReturn(List.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(paymentCardDTO);

        List<PaymentCardDTO> result = paymentCardsService.getAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(cardId);
        verify(paymentCardsRepository, times(1)).findAllByUserId(userId);
        verify(paymentCardMapper, times(1)).toDto(card);
    }

    @Test
    void getAllByUserId_shouldThrow_whenIdNull()
    {
        List<PaymentCardDTO> result = paymentCardsService.getAllByUserId(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getAllByUserId_shouldReturnEmptyList_whenNoCards()
    {
        when(paymentCardsRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<PaymentCardDTO> result = paymentCardsService.getAllByUserId(userId);

        assertThat(result).isEmpty();
        verify(paymentCardsRepository, times(1)).findAllByUserId(userId);
    }
}