package com.java.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "cv")
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

    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Column(name = "contact_information", nullable = false)
    @NotBlank(message = "Contact information is required")
    private String contactInformation;

    @Column(name = "summary_objective", nullable = false)
    @NotBlank(message = "Summary/Objective is required")
    private String summaryObjective;

    @Column(name = "soft_skills", nullable = false)
    @NotBlank(message = "Soft skills are required")
    private String softSkills;

    @Column(name = "education_history", nullable = false)
    @NotBlank(message = "Education history is required")
    private String educationHistory;

    @Column(name = "hard_skills", nullable = false)
    @NotBlank(message = "Hard skills are required")
    private String hardSkills;

    @Column(name = "certifications")
    private String certifications;

    @Column(name = "projects")
    private String projects;

    @Column(name = "awards_achievements")
    private String awardsAchievements;

    @Column(name = "references_info")
    private String referencesInfo;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @OneToMany
    private List<JobExperience> jobExperience;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}

