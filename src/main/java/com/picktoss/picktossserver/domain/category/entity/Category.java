package com.picktoss.picktossserver.domain.category.entity;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.CategoryTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "orders")
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    // Constructor methods
    public static Category createCategory(Member member, String name, CategoryTag tag, int order, String emoji) {
        Category category = Category.builder()
                .name(name)
                .member(member)
                .tag(tag)
                .order(order)
                .emoji(emoji)
                .build();

//        category.setMember(member);
        return category;
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

    public void updateCategoryTag(CategoryTag tag) {
        this.tag = tag;
    }

    public void updateCategoryOrder(int order) {
        this.order = order;
    }

    public void updateCategoryEmoji(String emoji) {
        this.emoji = emoji;
    }

    public void sortCategory() {

    }
}
