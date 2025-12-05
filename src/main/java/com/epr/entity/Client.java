// src/main/java/com/epr/entity/Client.java
package com.epr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String uuid;

    @NotBlank(message = "Client name is required")
    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 255)
    private String logo;

    @Column(length = 255)
    private String websiteUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String slug;

    // 1 = Visible, 2 = Hidden
    @Column(length = 2, columnDefinition = "varchar(2) default '1'")
    private String displayStatus = "1";

    @Column(length = 100)
    private String addedByUUID;

    @Column(length = 100)  // ← ADD THIS FIELD
    private String modifyByUUID;  // ← ADD THIS FIELD

    private LocalDateTime postDate;
    private LocalDateTime modifyDate;

    @Column(columnDefinition = "int default 2 COMMENT '1 deleted, 2 not deleted'")
    private int deleteStatus = 2;
}