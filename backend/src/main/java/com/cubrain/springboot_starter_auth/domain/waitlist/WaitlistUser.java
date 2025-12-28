package com.cubrain.springboot_starter_auth.domain.waitlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.cubrain.springboot_starter_auth.global.config.audit.BaseTimeEntity;

@Entity
@Table(name = "waitlist_users")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class WaitlistUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;
}