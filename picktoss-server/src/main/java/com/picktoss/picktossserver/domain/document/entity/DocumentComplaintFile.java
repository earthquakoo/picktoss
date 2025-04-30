package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "document_complaint_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentComplaintFile extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_complaint_id", nullable = false)
    private DocumentComplaint documentComplaint;


    public static DocumentComplaintFile createDocumentComplaintFile(String s3Key, DocumentComplaint documentComplaint) {
        return DocumentComplaintFile.builder()
                .s3Key(s3Key)
                .documentComplaint(documentComplaint)
                .build();
    }
}
