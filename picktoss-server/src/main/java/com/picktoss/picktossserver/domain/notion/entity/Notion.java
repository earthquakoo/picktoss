//package com.picktoss.picktossserver.domain.notion.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Getter
//@Table(name = "notion")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//public class Notion {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;
//
//    @Column(name = "access_token")
//    private String accessToken;
//
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false)
//    private Member member;
//}
