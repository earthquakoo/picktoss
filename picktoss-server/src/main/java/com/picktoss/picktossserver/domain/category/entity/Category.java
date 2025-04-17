package com.picktoss.picktossserver.domain.category.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "emoji", nullable = false)
    private String emoji;

    @Column(name = "orders", nullable = false)
    private Integer orders;

    public static Category createCategory(String name, String emoji, Integer orders) {
        return Category.builder()
                .name(name)
                .emoji(emoji)
                .orders(orders)
                .build();
    }
}
