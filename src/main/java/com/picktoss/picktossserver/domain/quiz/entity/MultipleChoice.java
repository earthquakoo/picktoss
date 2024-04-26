package com.picktoss.picktossserver.domain.quiz.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class MultipleChoice extends Quiz {

    @Column(name = "options", columnDefinition = "TEXT")
    private List<String> options;
}
