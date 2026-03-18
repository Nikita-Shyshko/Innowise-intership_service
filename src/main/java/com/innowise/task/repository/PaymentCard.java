package com.innowise.task.repository;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.dto.PaymentCardRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PaymentCard
{
    PaymentCardDTO create(PaymentCardRequestDTO RequestDto);
    PaymentCardDTO getById(Long id);
    Page<PaymentCardDTO> getAll(Specification<com.innowise.task.entity.PaymentCard> specification, Pageable pageable);
    void delete(Long id);
    PaymentCardDTO updatePaymentCardsById(Long id, PaymentCardDTO dtoPaymentCards);
    void setActiveStatus(Long id, boolean status);
    List<PaymentCardDTO> getAllByUserId(Long userId);
}