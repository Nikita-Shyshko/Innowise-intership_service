package com.innowise.task.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@MappedSuperclass
@EnableJpaAuditing
@Data
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditing
{
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime CreatedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime UpdatedAt;
}

