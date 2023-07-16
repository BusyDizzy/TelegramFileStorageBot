package com.java.entity;

import com.java.entity.enums.PromptType;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gpt_prompts")
public class ChatGPTPrompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private PromptType type;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}