package com.innowise.task.service;

import com.innowise.task.repository.PaymentCardRepository;
import com.innowise.task.repository.UserRepository;
import com.innowise.task.entity.PaymentCard;
import com.innowise.task.entity.User;
import com.innowise.task.exceptions.TooManyCardsForUserException;
import com.innowise.task.exceptions.NotFoundException;
import com.innowise.task.util.ExceptionMessages;
import com.innowise.task.exceptions.ValidationException;
import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.dto.PaymentCardRequestDTO;
import com.innowise.task.mapper.PaymentCardMapper;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PaymentCardServiceImpl
{
    private final PaymentCardRepository paymentCardsRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final PaymentCardMapper paymentCardMapper;

    @Caching(
            put = @CachePut(value = "cards", key = "#result.id"),
            evict = @CacheEvict(value = "cardsByUserId", key = "#paymentCardDTO.userId")
    )
    public PaymentCardDTO create(@NotNull(message = ExceptionMessages.CARD_DTO_MUST_NOT_BE_NULL) @Valid PaymentCardRequestDTO paymentCardDTO)
    {
        User users = userRepository.getUsersById(paymentCardDTO.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        List<PaymentCard> allUsersCards = paymentCardsRepository.findAllByUserId(paymentCardDTO.getUserId());
        if (allUsersCards.size() >= 5)
        {
            throw new TooManyCardsForUserException(ExceptionMessages.USER_ID_MUST_NOT_HAVE_MORE_THAN_FIVE_CARDS);
        }

        PaymentCard paymentCards = paymentCardMapper.toEntityFromCreateRequest(paymentCardDTO);
        paymentCards.setUser(users);
        paymentCards.setActive(true);

        PaymentCard savedCards = paymentCardsRepository.save(paymentCards);
        log.debug("The card with ID: {} has been created", savedCards.getId());
        return paymentCardMapper.toDto(savedCards);
    }

    @Cacheable(value = "cards", key = "#id")
    public PaymentCardDTO getById(@NotNull(message = ExceptionMessages.CARD_ID_MUST_NOT_BE_NULL) Long id)
    {
        log.debug("Fetching card by id: {}", id);
        return paymentCardsRepository.getPaymentCardsById(id)
                .map(paymentCardMapper::toDto)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.CARD_NOT_FOUND + id));
    }

    public Page<PaymentCardDTO> getAll(Specification<PaymentCard> specification, Pageable pageable)
    {
        log.debug("Received page number of cards {}", pageable.getPageNumber());
        return paymentCardsRepository.findAll(specification, pageable)
                .map(paymentCardMapper::toDto);
    }

    @Caching(
            put = @CachePut(value = "cards", key = "#id"),
            evict = @CacheEvict(value = "cardsByUserId", key = "#paymentCardDTO.userId")
    )

    public PaymentCardDTO updatePaymentCardsById(
            @NotNull(message = ExceptionMessages.CARD_ID_MUST_NOT_BE_NULL) Long id,
            @NotNull(message = ExceptionMessages.CARD_DTO_MUST_NOT_BE_NULL) @Valid PaymentCardDTO paymentCardDTO)
    {
        PaymentCard card = paymentCardsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.CARD_NOT_FOUND + id));

        card.setNumber(paymentCardDTO.getNumber());
        card.setHolder(paymentCardDTO.getHolder());

        PaymentCard updatedCard = paymentCardsRepository.save(card);
        log.info("Card with id={} has been updated", id);
        return paymentCardMapper.toDto(updatedCard);
    }

    public void delete(@NotNull(message = ExceptionMessages.CARD_ID_MUST_NOT_BE_NULL) Long id)
    {
        PaymentCard card = paymentCardsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.CARD_NOT_FOUND + id));
        Long userId = card.getUser().getId();

        paymentCardsRepository.delete(card);

        Cache cardsCache = cacheManager.getCache("cards");
        if (cardsCache != null)
        {
            cardsCache.evict(id);
        }
        Cache cardsByUserCache = cacheManager.getCache("cardsByUserId");
        if (cardsByUserCache != null)
        {
            cardsByUserCache.evict(userId);
        }

        log.info("The card has been successfully removed");
    }

    public void setActiveStatus(@NotNull(message = ExceptionMessages.CARD_ID_MUST_NOT_BE_NULL) Long id, boolean status)
    {
        PaymentCard card = paymentCardsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.CARD_NOT_FOUND + id));
        Long userId = card.getUser().getId();

        int setStatus = paymentCardsRepository.setActiveStatusOfPaymentCards(id, status);
        if (setStatus == 0)
        {
            throw new NotFoundException(ExceptionMessages.CARD_NOT_UPDATED + id);
        }
        Cache cardsCache = cacheManager.getCache("cards");
        if (cardsCache != null)
        {
            cardsCache.evict(id);
        }
        Cache cardsByUserCache = cacheManager.getCache("cardsByUserId");
        if (cardsByUserCache != null)
        {
            cardsByUserCache.evict(userId);
        }

        log.info("The card status has been successfully changed");
    }

    @Cacheable(value = "cardsByUserId", key = "#id")
    public List<PaymentCardDTO> getAllByUserId(@NotNull(message = ExceptionMessages.USER_ID_MUST_NOT_BE_NULL) Long id)
    {
        log.debug("Fetching cards for user id: {} from database", id);
        return paymentCardsRepository.findAllByUserId(id)
                .stream()
                .map(paymentCardMapper::toDto)
                .collect(Collectors.toList());
    }
}