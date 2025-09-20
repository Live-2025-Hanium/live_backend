package com.example.live_backend.infra.kakao.feign;

import com.example.live_backend.infra.kakao.feign.dto.KakaoCategoryResponse;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "kakaoLocal",
    url = "${kakao.local.base-url:https://dapi.kakao.com}",
    configuration = KakaoLocalFeignConfig.class
)
public interface KakaoLocalFeign {

    @GetMapping("/v2/local/search/keyword.json")
    KakaoKeywordResponse searchByKeyword(
        @RequestParam("query") String query,
        @RequestParam("x") double lng,
        @RequestParam("y") double lat,
        @RequestParam("radius") int radius,
        @RequestParam("page") int page,
        @RequestParam("size") int size,
        @RequestParam(value = "sort", required = false) String sort
    );

    @GetMapping("/v2/local/search/category.json")
    KakaoCategoryResponse searchByCategory(
        @RequestParam("category_group_code") String categoryGroupCode,
        @RequestParam("x") double lng,
        @RequestParam("y") double lat,
        @RequestParam("radius") int radius,
        @RequestParam("page") int page,
        @RequestParam("size") int size
    );
}