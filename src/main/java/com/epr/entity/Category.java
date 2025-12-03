package com.epr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String uuid;

    @Column(length = 100, unique = true, nullable = false)
    private String name;

    @Column(length = 200, unique = true, nullable = false)
    private String slug;

    @Column(length = 255)
    private String icon;

    @Column(length = 2, columnDefinition = "varchar(2) default '1'")
    private Integer displayStatus = 1; // 1=show, 2=hide

    @Column(length = 2, columnDefinition = "varchar(2) default '2'")
    private Integer showHomeStatus = 2; // 1=show on home, 2=don't

    // SEO Fields
    private String metaTitle;
    @Column(columnDefinition = "TEXT")
    private String metaKeyword;
    @Column(columnDefinition = "TEXT")
    private String metaDescription;
    @Column(columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(name = "post_date", nullable = false)
    private LocalDateTime postDate;

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "added_by_uuid", length = 100)
    private String addedByUUID;

    @Column(name = "modify_by_uuid", length = 100)
    private String modifyByUUID;

    @Column(columnDefinition = "int default 2")
    private int deleteStatus = 2; // 1=deleted, 2=active

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subcategory> subcategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Services> services = new ArrayList<>();
}