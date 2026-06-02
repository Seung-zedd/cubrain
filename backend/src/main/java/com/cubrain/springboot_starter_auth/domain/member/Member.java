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
import java.time.OffsetDateTime;
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

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

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

    @Column
    private OffsetDateTime endsAt;

    @Column
    private String customerPortalUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SubscriptionStatus subscriptionStatus;

    public boolean isProAccess() {
        return tier == UserTier.PRO_USER && (endsAt == null || endsAt.isAfter(OffsetDateTime.now()));
    }

    public void updateFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public void updateTier(UserTier tier) {
        this.tier = tier;
    }

    public void updateEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public void updateCustomerPortalUrl(String customerPortalUrl) {
        this.customerPortalUrl = customerPortalUrl;
    }

    public void updateSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
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

    public void decrementUploadCount() {
        if (this.dailyUploadCount > 0) {
            this.dailyUploadCount--;
        }
    }
}
