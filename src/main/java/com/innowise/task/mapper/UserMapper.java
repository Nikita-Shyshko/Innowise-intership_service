package com.innowise.task.mapper;

import com.innowise.task.dto.UserRequestDTO;
import com.innowise.task.entity.User;
import com.innowise.task.dto.UserDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper
{
    UserDTO toDTO(User users);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "paymentCards", ignore = true)
    User toEntity(UserDTO userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "paymentCards", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toEntityFromCreateRequest(UserRequestDTO requestDTO);
}
