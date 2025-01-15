package com.picktoss.picktossserver.domain.admin.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter
@Table(name = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Admin extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    public static Admin createAdmin(String name, String password) {
        return Admin.builder()
                .name(name)
                .password(password)
                .build();
    }
}
