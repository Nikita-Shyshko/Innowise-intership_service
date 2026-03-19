package com.innowise.task.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardDTO
{
    private Long id;
    private Long userId;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active;
}
