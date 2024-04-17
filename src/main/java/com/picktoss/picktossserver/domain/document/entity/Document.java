package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.question.entity.Question;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.DocumentFormat;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Document extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private DocumentFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();
}
