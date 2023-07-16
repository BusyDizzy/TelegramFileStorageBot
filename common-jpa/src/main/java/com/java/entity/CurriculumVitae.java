package com.java.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "curriculum_vitae")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumVitae {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "full_name")
    @NotBlank(message = "Full name is required")
    private String name;

    @Column(name = "position")
    @NotBlank(message = "Full name is required")
    private String position;

    @Column(name = "email")
    @NotBlank(message = "Contact information is required")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "hard_skills", columnDefinition = "TEXT")
    @NotBlank(message = "Hard skills are required")
    private String hardSkills;

    @Column(name = "soft_skills", columnDefinition = "TEXT")
    @NotBlank(message = "Hard skills are required")
    private String softSkills;

    @Column(name = "years_of_experience")
    private String yearsOfExperience;
    @Column(name = "experience_description", columnDefinition = "TEXT")
    private String experienceDescription;

    @Column(name = "education")
    @NotBlank(message = "Education history is required")
    private String education;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    public CurriculumVitae(String name, String position, String email, String phone, String hardSkills, String softSkills,
                           String yearsOfExperience, String experienceDescription, String education, AppUser appUser) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.position = position;
        this.hardSkills = hardSkills;
        this.softSkills = softSkills;
        this.yearsOfExperience = yearsOfExperience;
        this.experienceDescription = experienceDescription;
        this.education = education;
        this.appUser = appUser;
    }

}

