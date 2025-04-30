package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.document.ComplaintReason;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "document_complaint")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentComplaint extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_reason", nullable = false)
    private ComplaintReason complaintReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @OneToMany(mappedBy = "documentComplaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentComplaintFile> documentComplaintFiles = new ArrayList<>();


    public static DocumentComplaint createDocumentComplaint(String content, ComplaintReason complaintReason, Member member, Document document) {
        return DocumentComplaint.builder()
                .content(content)
                .complaintReason(complaintReason)
                .member(member)
                .document(document)
                .build();
    }
}
