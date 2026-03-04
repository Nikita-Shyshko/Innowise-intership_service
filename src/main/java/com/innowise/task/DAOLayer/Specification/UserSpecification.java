package com.innowise.task.DAOLayer.Specification;

import com.innowise.task.Entity.Users;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification
{
    public static Specification<Users> availabilityOfName(String name)
    {
        return ((root, query, criteriaBuilder) -> {
            if(name == null || name.isBlank())
            {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.upper(root.get("name")),
                    "%" + name.toUpperCase() + "%"
            );
        });
    }

    public static Specification<Users> availabilityOfSurname(String surname)
    {
        return (root, query, cb) -> {
            if (surname == null || surname.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.upper(root.get("surname")),
                    "%" + surname.toUpperCase() + "%"
            );
        };
    }
}
