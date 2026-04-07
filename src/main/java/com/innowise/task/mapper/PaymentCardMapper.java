package com.innowise.task.mapper;

import com.innowise.task.entity.PaymentCard;
import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.dto.PaymentCardRequestDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PaymentCardMapper
{

    @Mapping(source = "user.id", target = "userId")
    PaymentCardDTO toDto(PaymentCard paymentCards);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    PaymentCard toEntity(PaymentCardDTO paymentCardDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    PaymentCard toEntityFromCreateRequest(PaymentCardRequestDTO requestDTO);
}