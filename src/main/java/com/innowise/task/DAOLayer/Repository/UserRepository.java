package com.innowise.task.DAOLayer.Repository;

import com.innowise.task.Entity.Users;
import com.innowise.task.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users>
{
    @Query("select r from Users r where r.id = :id")
    Optional<Users> getUsersById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Users SET name = :name, surname = :surname, email = :email WHERE id = :id")
    int updateUserById(@Param("id") Long id,
                       @Param("name") String name,
                       @Param("surname") String surname,
                       @Param("email") String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Users r set r.active = :active where r.id = :id")
    int setActiveStatusOfUsers(@Param("id") Long id,
                               @Param("active") UserStatus active);
}