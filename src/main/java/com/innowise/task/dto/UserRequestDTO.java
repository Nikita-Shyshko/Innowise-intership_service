package com.innowise.task.dto;

import com.innowise.task.entity.UserStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequestDTO
{
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
}
