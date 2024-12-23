package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "collection_complaint_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionComplaintFile extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_complaint_id", nullable = false)
    private CollectionComplaint collectionComplaint;

    public static CollectionComplaintFile createCollectionComplaintFile(String s3Key, CollectionComplaint collectionComplaint) {
        return CollectionComplaintFile.builder()
                .s3Key(s3Key)
                .collectionComplaint(collectionComplaint)
                .build();
    }
}
