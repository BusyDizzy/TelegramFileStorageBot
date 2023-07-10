package com.java.DTO;

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
