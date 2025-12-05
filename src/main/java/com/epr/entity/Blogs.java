// src/main/java/com/epr/entity/Blogs.java

package com.epr.entity;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "blogs",
        indexes = {
                @Index(name = "idx_blogs_public", columnList = "deleteStatus, displayStatus, postDate DESC"),
                @Index(name = "idx_blogs_slug", columnList = "slug", unique = true),
                @Index(name = "idx_blogs_category", columnList = "category_id, deleteStatus, displayStatus"),
                @Index(name = "idx_blogs_subcategory", columnList = "subcategory_id"),
                @Index(name = "idx_blogs_home", columnList = "showHomeStatus, deleteStatus, displayStatus, postDate"),
                @Index(name = "idx_blogs_visited", columnList = "visited DESC")
                // FULLTEXT index added manually later — removed from here
        }
)
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
public class Blogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false, updatable = false)
    private String uuid;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String title;

    @NotBlank
    @Size(max = 300)
    @Column(unique = true, length = 300, nullable = false)
    private String slug;

    @Column(length = 500)
    private String image;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @NotBlank
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String description;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String metaTitle;

    @Column(columnDefinition = "TEXT")
    private String metaKeyword;

    @Column(columnDefinition = "TEXT")
    private String metaDescription;

    // === STATUS FIELDS (NO DUPLICATE DEFAULTS!) ===
    @Column(nullable = false, columnDefinition = "tinyint default 1")
    private Integer displayStatus = 1;

    @Column(nullable = false, columnDefinition = "tinyint default 2")
    private int deleteStatus = 2;

    @Column(name = "show_home_status", nullable = false, columnDefinition = "tinyint default 2")
    private Integer showHomeStatus = 2;

    @CreationTimestamp
    @Column(name = "post_date", nullable = false, updatable = false)
    private LocalDateTime postDate;

    @UpdateTimestamp
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(length = 100)
    private String addedByUUID;

    @Column(length = 100)
    private String modifyByUUID;

    @Column(length = 100, nullable = false)
    private String postedByUuid;

    @Column(length = 100)
    private String postedByName;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long visited = 0L;

    @Column(columnDefinition = "TEXT")
    private String searchKeyword;

    // === RELATIONSHIPS ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "service_blogs",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"),
            indexes = {
                    @Index(name = "idx_service_blogs_service", columnList = "service_id"),
                    @Index(name = "idx_service_blogs_blog", columnList = "blog_id")
            }
    )
    private List<Services> services = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BlogFaq> faqs = new ArrayList<>();

    public boolean isActive() {
        return deleteStatus == 2 && displayStatus == 1;
    }

}