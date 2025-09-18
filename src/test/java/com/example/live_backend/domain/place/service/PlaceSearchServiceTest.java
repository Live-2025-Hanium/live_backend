package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.domain.place.mapper.PlaceCategoryMapper;
import com.example.live_backend.domain.place.mapper.PlaceConverter;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoCategoryResponse;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceSearchService 단위 테스트")
class PlaceSearchServiceTest {

    @Mock
    private KakaoLocalFeign kakaoLocalFeign;
    
    @Mock
    private PlaceCategoryMapper categoryMapper;
    
    @Mock
    private PlaceConverter placeConverter;

    @InjectMocks
    private PlaceSearchService placeSearchService;

    @Nested
    @DisplayName("키워드 검색")
    class SearchByKeyword {

        @Test
        @DisplayName("성공 - 검색 결과를 PageTemplate로 변환")
        void searchSuccess() {
            // Given
            SearchRequest request = SearchRequest.builder()
                .query("카페")
                .lng(127.0)
                .lat(37.5)
                .radius(1000)
                .page(1)
                .size(10)
                .build();

            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(new ArrayList<>(), new KakaoKeywordResponse.Meta());
            PageTemplate<PlaceItem> expectedPage = new PageTemplate<>(10L, 1, 1, 10, false, new ArrayList<>());
            
            when(kakaoLocalFeign.searchByKeyword(
                eq("카페"), eq(127.0), eq(37.5), eq(1000), eq(1), eq(10), isNull()))
                .thenReturn(kakaoResponse);
            
            when(placeConverter.toPageTemplate(any(KakaoKeywordResponse.class), eq(1), eq(10)))
                .thenReturn(expectedPage);

            // When
            PageTemplate<PlaceItem> result = placeSearchService.searchByKeyword(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.totalElements()).isEqualTo(10L);
            verify(kakaoLocalFeign).searchByKeyword("카페", 127.0, 37.5, 1000, 1, 10, null);
            verify(placeConverter).toPageTemplate(kakaoResponse, 1, 10);
        }

        @Test
        @DisplayName("실패 처리 - API 오류시 빈 결과 반환")
        void apiErrorReturnsEmpty() {
            // Given
            SearchRequest request = SearchRequest.builder()
                .query("카페")
                .lng(127.0)
                .lat(37.5)
                .radius(1000)
                .page(1)
                .size(10)
                .build();
            
            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(),
                anyInt(), anyInt(), anyInt(), isNull()))
                .thenThrow(FeignException.errorStatus("searchByKeyword",
                    Response.builder()
                        .status(502)
                        .reason("Bad Gateway")
                        .request(Request.create(Request.HttpMethod.GET, "url",
                            new java.util.HashMap<>(), null, java.nio.charset.StandardCharsets.UTF_8))
                        .build()));

            // When & Then
            assertThatThrownBy(() -> placeSearchService.searchByKeyword(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_ERROR);
        }

        @Test
        @DisplayName("정렬 옵션이 API 호출에 전달됨")
        void sortOptionPassed() {
            // Given
            SearchRequest request = SearchRequest.builder()
                .query("카페")
                .lng(127.0)
                .lat(37.5)
                .radius(1000)
                .page(1)
                .size(10)
                .sort("distance")
                .build();
            
            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(new ArrayList<>(), new KakaoKeywordResponse.Meta());
            PageTemplate<PlaceItem> expectedPage = new PageTemplate<>(0L, 0, 1, 10, false, new ArrayList<>());
            
            when(kakaoLocalFeign.searchByKeyword(
                anyString(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt(), eq("distance")))
                .thenReturn(kakaoResponse);
            
            when(placeConverter.toPageTemplate(any(), anyInt(), anyInt()))
                .thenReturn(expectedPage);

            // When
            placeSearchService.searchByKeyword(request);

            // Then
            verify(kakaoLocalFeign).searchByKeyword("카페", 127.0, 37.5, 1000, 1, 10, "distance");
        }
    }

    @Nested
    @DisplayName("카테고리 검색")
    class SearchByCategory {

        @Test
        @DisplayName("성공 - 카테고리 매핑 후 검색")
        void categoryMappingSuccess() {
            // Given
            NearbyRequest request = NearbyRequest.builder()
                .category("PSY")
                .lng(127.0)
                .lat(37.5)
                .radius(1000)
                .page(1)
                .size(10)
                .build();

            KakaoCategoryResponse kakaoResponse = new KakaoCategoryResponse(new ArrayList<>(), new KakaoKeywordResponse.Meta());
            PageTemplate<PlaceItem> expectedPage = new PageTemplate<>(5L, 1, 1, 10, false, new ArrayList<>());
            
            when(categoryMapper.toKakaoCategory("PSY"))
                .thenReturn("HP8");
            
            when(kakaoLocalFeign.searchByCategory(
                eq("HP8"), eq(127.0), eq(37.5), eq(1000), eq(1), eq(10)))
                .thenReturn(kakaoResponse);
            
            when(placeConverter.toPageTemplate(any(KakaoCategoryResponse.class), 
                eq(1), eq(10), eq("PSY")))
                .thenReturn(expectedPage);

            // When
            PageTemplate<PlaceItem> result = placeSearchService.searchByCategory(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.totalElements()).isEqualTo(5L);
            verify(categoryMapper).toKakaoCategory("PSY");
            verify(kakaoLocalFeign).searchByCategory("HP8", 127.0, 37.5, 1000, 1, 10);
        }

        @Test
        @DisplayName("실패 - 잘못된 카테고리")
        void invalidCategoryThrowsException() {
            // Given
            NearbyRequest request = NearbyRequest.builder()
                .category("INVALID")
                .lng(127.0)
                .lat(37.5)
                .radius(1000)
                .page(1)
                .size(10)
                .build();
            
            when(categoryMapper.toKakaoCategory("INVALID"))
                .thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> placeSearchService.searchByCategory(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CATEGORY);
        }

        @Test
        @DisplayName("정신과 필터링 적용")
        void psychiatryFiltering() {
            // Given
            NearbyRequest request = NearbyRequest.builder()
                .category("PSY")
                .lng(127.0)
                .lat(37.5)
                .radius(1000)
                .page(1)
                .size(10)
                .build();

            List<KakaoKeywordResponse.KakaoPlace> mixedPlaces = new ArrayList<>();
            KakaoKeywordResponse.KakaoPlace psychiatry = mock(KakaoKeywordResponse.KakaoPlace.class);
            when(psychiatry.getCategoryName()).thenReturn("의료,건강 > 병원 > 정신건강의학과");
            
            KakaoKeywordResponse.KakaoPlace general = mock(KakaoKeywordResponse.KakaoPlace.class);
            when(general.getCategoryName()).thenReturn("의료,건강 > 병원 > 내과");
            
            mixedPlaces.add(psychiatry);
            mixedPlaces.add(general);
            
            KakaoCategoryResponse kakaoResponse = new KakaoCategoryResponse(
                mixedPlaces, 
                new KakaoKeywordResponse.Meta()
            );
            
            when(categoryMapper.toKakaoCategory("PSY")).thenReturn("HP8");
            when(categoryMapper.isPsychiatryCategory("PSY")).thenReturn(true);
            when(kakaoLocalFeign.searchByCategory(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt())).thenReturn(kakaoResponse);
            
            PageTemplate<PlaceItem> expectedPage = new PageTemplate<>(1L, 1, 1, 10, false, new ArrayList<>());
            when(placeConverter.toPageTemplate(any(KakaoCategoryResponse.class), 
                anyInt(), anyInt(), anyString())).thenReturn(expectedPage);

            // When
            PageTemplate<PlaceItem> result = placeSearchService.searchByCategory(request);

            // Then
            assertThat(result).isNotNull();
            verify(categoryMapper).isPsychiatryCategory("PSY");

            verify(placeConverter).toPageTemplate(
                argThat(response -> {

                    return response.getDocuments().stream()
                        .allMatch(place -> place.getCategoryName().contains("정신"));
                }), 
                eq(1), eq(10), eq("PSY")
            );
        }

        @Test
        @DisplayName("실패 처리 - API 오류시 빈 결과 반환")
        void apiErrorReturnsEmpty() {
            // Given
            NearbyRequest request = NearbyRequest.builder()
                .category("PSY")
                .lng(127.0)
                .lat(37.5)
                .radius(1000)
                .page(1)
                .size(10)
                .build();
            
            when(categoryMapper.toKakaoCategory("PSY"))
                .thenReturn("HP8");
            
            when(kakaoLocalFeign.searchByCategory(anyString(), anyDouble(), anyDouble(),
                anyInt(), anyInt(), anyInt()))
                .thenThrow(FeignException.errorStatus("searchByCategory",
                    Response.builder()
                        .status(502)
                        .reason("Bad Gateway")
                        .request(Request.create(Request.HttpMethod.GET, "url",
                            new java.util.HashMap<>(), null, java.nio.charset.StandardCharsets.UTF_8))
                        .build()));

            // When & Then
            assertThatThrownBy(() -> placeSearchService.searchByCategory(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}