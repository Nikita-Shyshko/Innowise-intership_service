package com.innowise.task.DAOLayer.Specification;

import com.innowise.task.Entity.PaymentCards;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification
{
    public static Specification<PaymentCards> availabilityOfName(String name)
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

    public static Specification<PaymentCards> availabilityOfSurname(String surname)
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
