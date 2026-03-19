package com.innowise.task.dto;

import com.innowise.task.entity.UserStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO
{
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private UserStatus active;
}
