package com.innowise.task.dto;

import lombok.Data;

import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentCardFilterDTO extends PaginationDTO
{
    private String name;
    private String surname;

    public PaymentCardFilterDTO(String name, String surname, Integer pageNumber, Integer pageSize, String sortedBy)
    {
        super(pageNumber, pageSize, sortedBy);
        this.name = name;
        this.surname = surname;
    }
}
