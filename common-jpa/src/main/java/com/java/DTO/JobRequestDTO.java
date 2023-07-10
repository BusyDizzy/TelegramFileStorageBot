package com.java.DTO;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDTO {
    private String query;
    private String location;
}