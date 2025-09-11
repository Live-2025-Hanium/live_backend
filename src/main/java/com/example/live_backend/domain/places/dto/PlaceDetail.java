package com.example.live_backend.domain.places.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장소 상세 정보")
public class PlaceDetail {
    
    @Schema(description = "장소 ID", example = "kakao:123456789")
    private String id;
    
    @Schema(description = "장소명", example = "더마음의원")
    private String name;
    
    @Schema(description = "카테고리 정보")
    private Category category;
    
    @Schema(description = "주소 정보")
    private Address address;
    
    @Schema(description = "위치 좌표")
    private Location location;
    
    @Schema(description = "전화번호", example = "02-3242-3242")
    private String phone;
    
    @Schema(description = "운영 시간")
    private List<Hour> hours;
    
    @Schema(description = "소개", example = "정신건강 전문의 진료...")
    private String intro;
    
    @Schema(description = "사진 URL 목록")
    private List<String> photos;
    
    @Schema(description = "데이터 소스", example = "kakao")
    private String source;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "운영 시간")
    public static class Hour {
        
        @Schema(description = "요일", example = "월")
        private String day;
        
        @Schema(description = "오픈 시간", example = "10:00")
        private String open;
        
        @Schema(description = "마감 시간", example = "19:00")
        private String close;
    }
}