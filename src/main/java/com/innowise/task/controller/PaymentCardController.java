package com.innowise.task.controller;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.dto.PaymentCardFilterDTO;
import com.innowise.task.dto.PaymentCardRequestDTO;
import com.innowise.task.service.PaymentCardServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/cards")
public class PaymentCardController
{
    private final PaymentCardServiceImpl paymentCardService;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> getById(@PathVariable Long id)
    {
        return ResponseEntity.ok(paymentCardService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentCardDTO> createCard(@RequestBody @Valid PaymentCardRequestDTO cards)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.create(cards));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> updateCardById(@PathVariable Long id,
                                                           @RequestBody @Valid PaymentCardDTO paymentCards)
    {
        return ResponseEntity.ok(paymentCardService.updatePaymentCardsById(id, paymentCards));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCardById(@PathVariable Long id)
    {
        paymentCardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> setActiveCardStatus(@PathVariable Long id, @RequestParam boolean status)
    {
        paymentCardService.setActiveStatus(id, status);
        return ResponseEntity.status(HttpStatus.OK).body(paymentCardService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDTO>> getCardsByUserId(@PathVariable Long userId)
    {
        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId));
    }

    @PostMapping("/all")
    public ResponseEntity<Page<PaymentCardDTO>> getAllCards(@RequestBody @Valid PaymentCardFilterDTO filter)
    {
        Page<PaymentCardDTO> cards = paymentCardService.getAll(filter);
        return ResponseEntity.ok(cards);
    }
}
