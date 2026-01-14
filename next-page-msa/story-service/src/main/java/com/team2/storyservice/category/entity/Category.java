package com.team2.storyservice.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 엔티티
 *
 * @author 정진호
 */
@Entity
@Getter
@Table(name = "categories")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @Column(name = "category_id", length = 20)
    private String categoryId; // THRILLER, ROMANCE 등

    @Column(name = "category_nm", nullable = false, length = 50)
    private String categoryName; // 스릴러, 로맨스 등
}
