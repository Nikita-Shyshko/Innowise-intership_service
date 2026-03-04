package com.innowise.task.repository;

import com.innowise.task.entity.PaymentCards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCards, Long>, JpaSpecificationExecutor<PaymentCards>
{
    @Query("select r from PaymentCards r where r.id = :id")
    Optional<PaymentCards> getPaymentCardsById(@Param("id") Long id);

    List<PaymentCards> findAllByUserId(Long id);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE payment_cards
        SET number = :number,
            holder = :holder
        WHERE id = :id
        """, nativeQuery = true)
    int updatePaymentCardsById(
            @Param("id") Long id,
            @Param("number") String number,
            @Param("holder") String holder);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PaymentCards r set r.active = :active where r.id = :id")
    int setActiveStatusOfPaymentCards(@Param("id") Long id,
                                      @Param("active") boolean active);
}