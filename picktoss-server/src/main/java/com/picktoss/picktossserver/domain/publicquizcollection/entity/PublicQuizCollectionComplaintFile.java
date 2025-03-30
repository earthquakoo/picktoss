package com.picktoss.picktossserver.domain.publicquizcollection.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "public_quiz_collection_complaint_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PublicQuizCollectionComplaintFile extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_quiz_collection_complaint_id", nullable = false)
    private PublicQuizCollectionComplaint publicQuizCollectionComplaint;

    public static PublicQuizCollectionComplaintFile createPublicQuizCollectionComplaintFile(String s3Key, PublicQuizCollectionComplaint publicQuizCollectionComplaint) {
        return PublicQuizCollectionComplaintFile.builder()
                .s3Key(s3Key)
                .publicQuizCollectionComplaint(publicQuizCollectionComplaint)
                .build();
    }
}
