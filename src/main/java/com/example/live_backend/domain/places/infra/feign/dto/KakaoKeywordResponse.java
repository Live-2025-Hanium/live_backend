package com.example.live_backend.domain.places.infra.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoKeywordResponse {

    @JsonProperty("documents")
    private List<KakaoPlace> documents;

    @JsonProperty("meta")
    private Meta meta;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoPlace {
        
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("place_name")
        private String placeName;
        
        @JsonProperty("category_name")
        private String categoryName;
        
        @JsonProperty("category_group_code")
        private String categoryGroupCode;
        
        @JsonProperty("category_group_name")
        private String categoryGroupName;
        
        @JsonProperty("phone")
        private String phone;
        
        @JsonProperty("address_name")
        private String addressName;
        
        @JsonProperty("road_address_name")
        private String roadAddressName;
        
        @JsonProperty("x")
        private String x;  // longitude
        
        @JsonProperty("y")
        private String y;  // latitude
        
        @JsonProperty("place_url")
        private String placeUrl;
        
        @JsonProperty("distance")
        private String distance;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        
        @JsonProperty("is_end")
        private boolean isEnd;
        
        @JsonProperty("pageable_count")
        private int pageableCount;
        
        @JsonProperty("total_count")
        private int totalCount;
        
        @JsonProperty("same_name")
        private SameName sameName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SameName {
        
        @JsonProperty("region")
        private List<String> region;
        
        @JsonProperty("keyword")
        private String keyword;
        
        @JsonProperty("selected_region")
        private String selectedRegion;
    }
}