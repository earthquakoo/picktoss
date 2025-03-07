package com.picktoss.picktossserver.domain.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;


@Table(name = "email_verification")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class EmailVerification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false)
    @NotBlank
    private String email;

    @Column(name = "verification_code", nullable = false)
    @NotBlank
    private String verificationCode;

    @Column(name = "is_verified", nullable = false)
    @NotNull
    private Boolean isVerified;

    @Column(name = "expiration_time", nullable = false)
    @NotNull
    private LocalDateTime expirationTime;

    /**
     * 비즈니스 로직
     **/
    public void verify() {
        isVerified = true;
    }

    public void unverify() {
        isVerified = false;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public boolean isValidVerificationCode(String verificationCode) {
        return this.verificationCode.equals(verificationCode);
    }

    public void updateVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public void renewExpirationTimeFromNow(long expireDurationSeconds) {
        this.expirationTime = LocalDateTime.now().plusSeconds(expireDurationSeconds);
    }
}
