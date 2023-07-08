package com.java.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
@Entity
@Table(name = "job_listing")
public class JobListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Job Title is required")
    @Column(name = "job_title")
    private String jobTitle;

    @NotEmpty(message = "Company Name is required")
    @Column(name = "company_name")
    private String companyName;

    @NotEmpty(message = "Location is required")
    @Column(name = "location")
    private String location;

    @NotEmpty(message = "Company Description is required")
    @Column(name = "company_description", columnDefinition = "TEXT")
    private String companyDescription;

    @NotEmpty(message = "Job Description is required")
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @NotEmpty(message = "Job Responsibilities is required")
    @Column(name = "job_responsibilities", columnDefinition = "TEXT")
    private String jobResponsibilities;

    @NotEmpty(message = "Job Qualifications is required")
    @Column(name = "job_qualifications", columnDefinition = "TEXT")
    private String jobQualifications;

    @NotEmpty(message = "Job Additional Skills is required")
    @Column(name = "job_additional_skills", columnDefinition = "TEXT")
    private String jobAdditionalSkills;

    @Positive(message = "Years of experience must be a positive number")
    @Column(name = "years_of_experience")
    private int yearsOfExperience;

    @NotEmpty(message = "Short list of Skills is required")
    @Column(name = "short_list_of_skills", columnDefinition = "TEXT")
    private String shortListOfSkills;

    @NotEmpty(message = "Employment Type is required")
    @Column(name = "employment_type")
    private String employmentType;

    @NotEmpty(message = "Industry is required")
    @Column(name = "industry")
    private String industry;

    @NotEmpty(message = "Salary/Compensation is required")
    @Column(name = "salary_compensation")
    private String salaryCompensation;

    @NotNull(message = "Application Deadline is required")
    @Column(name = "application_deadline")
    private Date applicationDeadline;

    @NotEmpty(message = "Contact Information is required")
    @Column(name = "contact_information")
    private String contactInformation;

    @PastOrPresent(message = "Posting Date must be a past or present date")
    @NotNull(message = "Posting Date is required")
    @Column(name = "posting_date")
    private Date postingDate;

    @Column(name = "other_details", columnDefinition = "TEXT")
    private String otherDetails;
}