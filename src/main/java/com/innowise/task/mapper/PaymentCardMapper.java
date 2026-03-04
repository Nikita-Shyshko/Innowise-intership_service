package com.innowise.task.mapper;

import com.innowise.task.entity.PaymentCards;
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
        nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)
public interface PaymentCardMapper
{

    @Mapping(source = "user.id", target = "userId")
    PaymentCardDTO toDto(PaymentCards paymentCards);

    @Mapping(source = "userId", target = "user.id")
    PaymentCards toEntity(PaymentCardDTO paymentCardDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "user", ignore = true)
    PaymentCards toEntityFromCreateRequest(PaymentCardRequestDTO requestDTO);
}