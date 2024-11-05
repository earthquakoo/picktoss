package com.picktoss.picktossserver.domain.directory.entity;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.directory.DirectoryTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "directory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Directory extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false)
    private DirectoryTag tag;

    @Column(name = "emoji")
    private String emoji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();

    // Constructor methods
    public static Directory createDirectory(Member member, String name, String emoji) {
        Directory directory = Directory.builder()
                .name(name)
                .tag(DirectoryTag.NORMAL)
                .member(member)
                .emoji(emoji)
                .build();

        return directory;
    }

    public static Directory createDefaultDirectory(Member member) {
        return Directory.builder()
                .name("기본 폴더")
                .tag(DirectoryTag.DEFAULT)
                .emoji(null)
                .member(member)
                .build();
    }

    // Business Logics
    public void updateDirectoryName(String name) {
        this.name = name;
    }

    public void updateDirectoryEmoji(String emoji) {
        this.emoji = emoji;
    }

}
