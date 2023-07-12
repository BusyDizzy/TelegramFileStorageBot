package com.java.entity;

import com.java.DTO.JobListingDTO;
import com.java.entity.enums.UserState;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramUserId;

    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Boolean isActive;
    @Enumerated(EnumType.STRING)
    private UserState state;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private List<CurriculumVitae> curriculumVitaeList;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private List<JobListingDTO> jobListingDTOList;

    public void addCurriculumVitae(CurriculumVitae curriculumVitae) {
        this.curriculumVitaeList.add(curriculumVitae);
        curriculumVitae.setAppUser(this);
    }

    public void removeCurriculumVitae(CurriculumVitae curriculumVitae) {
        this.curriculumVitaeList.remove(curriculumVitae);
        curriculumVitae.setAppUser(null);
    }

    public void addJobListingDTO(JobListingDTO jobListingDTO) {
        this.jobListingDTOList.add(jobListingDTO);
        jobListingDTO.setAppUser(this);
    }

    public void removeJobListingDTO(JobListingDTO jobListingDTO) {
        this.jobListingDTOList.remove(jobListingDTO);
        jobListingDTO.setAppUser(null);
    }
}