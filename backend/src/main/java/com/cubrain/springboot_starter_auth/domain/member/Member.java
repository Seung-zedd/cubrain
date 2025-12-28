package com.cubrain.springboot_starter_auth.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import java.time.LocalDate;
import com.cubrain.springboot_starter_auth.global.config.audit.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "members")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String supabaseId;

    @Builder.Default
    @Column(nullable = false)
    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTier tier;

    @Builder.Default
    @Column(nullable = false)
    private int dailyUploadCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private LocalDate lastUploadDate = LocalDate.now();

    public void updateSupabaseId(String supabaseId) {
        this.supabaseId = supabaseId;
    }

    public void updateTier(UserTier tier) {
        this.tier = tier;
    }

    public void incrementUploadCount() {
        resetCountIfNewDay();
        this.dailyUploadCount++;
    }

    public void resetCountIfNewDay() {
        if (this.lastUploadDate == null || !LocalDate.now().equals(this.lastUploadDate)) {
            this.dailyUploadCount = 0;
            this.lastUploadDate = LocalDate.now();
        }
    }
}
