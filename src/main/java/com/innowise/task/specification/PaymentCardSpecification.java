package com.innowise.task.specification;

import com.innowise.task.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification
{
    public static Specification<PaymentCard> availabilityOfName(String name)
    {
        return ((root, query, criteriaBuilder) -> {
            if(name == null || name.isBlank())
            {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.upper(root.join("user").get("name")),
                    "%" + name.toUpperCase() + "%"
            );
        });
    }

    public static Specification<PaymentCard> availabilityOfSurname(String surname)
    {
        return ((root, query, criteriaBuilder) -> {
            if(surname == null || surname.isBlank())
            {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.upper(root.join("user").get("surname")),
                    "%" + surname.toUpperCase() + "%"
            );
        });
    }
}
