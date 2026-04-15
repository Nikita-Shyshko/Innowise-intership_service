package com.innowise.task.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class PaymentCardRequestDTO implements Serializable
{
    private Long userId;
    private String number;
    private String holder;
    private LocalDate expirationDate;
}
