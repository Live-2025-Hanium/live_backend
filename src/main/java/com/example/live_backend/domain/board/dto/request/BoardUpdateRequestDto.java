package com.example.live_backend.domain.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BoardUpdateRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @Size(max = 100, message = "관련 기관은 100자를 초과할 수 없습니다.")
    private String relatedOrganization;

    private List<String> imageUrls;

    public BoardUpdateRequestDto(String title, String content, Long categoryId, 
                                String relatedOrganization, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.relatedOrganization = relatedOrganization;
        this.imageUrls = imageUrls;
    }
} 