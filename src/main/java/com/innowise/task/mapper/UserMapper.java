package com.innowise.task.mapper;

import com.innowise.task.entity.User;
import com.innowise.task.dto.UserDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)

public interface UserMapper
{
    UserDTO toDTO(User users);

    User toEntity(UserDTO userDto);
}
