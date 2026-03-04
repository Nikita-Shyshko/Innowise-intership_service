package com.innowise.task.mapper;

import com.innowise.task.entity.Users;
import com.innowise.task.dto.UserDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)

public interface UsersMapper
{
    UserDTO toDTO(Users users);

    Users toEntity(UserDTO userDto);
}
