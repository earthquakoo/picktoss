package com.picktoss.picktossserver.domain.category.entity;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.category.CategoryTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false)
    private CategoryTag tag;

    @Column(name = "emoji")
    private String emoji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();

    // Constructor methods
    public static Category createCategory(Member member, String name, String emoji) {
        Category category = Category.builder()
                .name(name)
                .tag(CategoryTag.NORMAL)
                .member(member)
                .emoji(emoji)
                .build();

//        category.setMember(member);
        return category;
    }

    public static Category createDefaultCategory(Member member) {
        return Category.builder()
                .name("기본 폴더")
                .tag(CategoryTag.DEFAULT)
                .emoji(null)
                .member(member)
                .build();
    }

    // 연관관계 메서드
    public void setMember(Member member) {
        this.member = member;
        member.getCategories().add(this);
    }

    // Business Logics
    public void updateCategoryName(String name) {
        this.name = name;
    }

    public void updateCategoryEmoji(String emoji) {
        this.emoji = emoji;
    }

}
