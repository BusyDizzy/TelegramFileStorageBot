package com.java.repository;

import com.java.entity.ChatGPTPrompt;
import com.java.entity.enums.PromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatGPTPromptsRepository extends JpaRepository<ChatGPTPrompt, Long> {
    @Query("Select g from ChatGPTPrompt g where g.appUser.id=:userId and g.type=:type")
    ChatGPTPrompt findByPromptType(@Param("userId") Long userId, @Param("type") PromptType type);
}