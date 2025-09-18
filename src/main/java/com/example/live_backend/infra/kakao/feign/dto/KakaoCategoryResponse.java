package com.example.live_backend.infra.kakao.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoCategoryResponse {

    @JsonProperty("documents")
    private List<KakaoKeywordResponse.KakaoPlace> documents;

    @JsonProperty("meta")
    private KakaoKeywordResponse.Meta meta;
}