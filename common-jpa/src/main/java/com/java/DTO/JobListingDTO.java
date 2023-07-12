package com.java.DTO;

import com.java.entity.AppUser;
import com.java.entity.enums.JobMatchState;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "downloaded_jobs")
public class JobListingDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private String webSiteJobId;
    @Column(length = 500)
    private String jobTitle;
    @Column(length = 500)
    private String companyName;
    @Column(columnDefinition = "LONGTEXT")
    private String jobDescription;
    @Column(length = 500)
    private String url;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
    @Enumerated(EnumType.STRING)
    private JobMatchState jobMatchState;

    private Boolean isCoverSend;

    private String contact;

    public JobListingDTO(Long id, String webSiteJobId, String jobTitle, String companyName,
                         String jobDescription, String url, JobMatchState jobMatchState, Boolean isCoverSend, Long appUserId) {
        this.id = id;
        this.webSiteJobId = webSiteJobId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.jobDescription = jobDescription;
        this.jobMatchState = jobMatchState;
        this.isCoverSend = isCoverSend;
        this.url = url;
        this.appUser = new AppUser();
        this.appUser.setId(appUserId);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobListingDTO that = (JobListingDTO) o;
        return Objects.equals(webSiteJobId, that.webSiteJobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webSiteJobId);
    }
}
