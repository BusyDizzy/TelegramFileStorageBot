package com.java.DTO;

import com.java.entity.AppUser;
import lombok.*;

import javax.persistence.*;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "curriculum_vitae")
public class CurriculumVitaeDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "TEXT")
    private String cvDescription;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}
