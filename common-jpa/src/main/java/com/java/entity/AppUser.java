package com.java.entity;

import com.java.DTO.JobListingDTO;
import com.java.entity.enums.Role;
import com.java.entity.enums.UserState;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    @Column(name = "is_cv_uploaded")
    private Boolean isCvUploaded;

    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role"}, name = "uk_user_role")})
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 200)
    @JoinColumn
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Role> roles;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private List<CurriculumVitae> curriculumVitaeList;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private List<JobListingDTO> jobListingDTOList;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private List<AppDocument> appDocuments;

    public void addDocument(AppDocument appDocument) {
        this.appDocuments.add(appDocument);
        appDocument.setAppUser(this);
    }

    public void removeDocument(AppDocument appDocument) {
        this.appDocuments.remove(appDocument);
        appDocument.setAppUser(null);
    }

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

    public void setRoles(Collection<Role> roles) {
        this.roles = CollectionUtils.isEmpty(roles) ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

}