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

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "orders", nullable = false)
    private Integer orders;

    @Column(name = "language", nullable = false)
    private String language;

    public static Category createCategory(String name, String emoji, String color, Integer orders, String language) {
        return Category.builder()
                .name(name)
                .emoji(emoji)
                .orders(orders)
                .color(color)
                .language(language)
                .build();
    }
}
