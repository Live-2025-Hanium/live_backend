package com.example.live_backend.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExampleRequestDto {
    
    @Schema(description = "제목", example = "예제 제목")
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;
    
    @Schema(description = "내용", example = "예제 내용입니다.")
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;
    
    @Schema(description = "작성자", example = "홍길동")
    @NotBlank(message = "작성자는 필수 입력값입니다.")
    private String author;
} 