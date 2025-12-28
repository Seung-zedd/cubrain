package com.cubrain.springboot_starter_auth.global.config.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(value = { AuditingEntityListener.class }) // To apply auditing
@MappedSuperclass // Used for common mapping information, provides mapping info to child classes
@Getter
public abstract class BaseTimeEntity {

    @CreatedDate // Automatically stores time when entity is created
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // Automatically stores time when entity is modified
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
