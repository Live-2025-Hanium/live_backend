package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SuggestRequest;
import com.example.live_backend.global.page.PageTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceService 통합 테스트")
class PlaceServiceTest {

    @Mock
    private PlaceSearchService placeSearchService;
    
    @Mock
    private PlaceDetailService placeDetailService;
    
    @Mock
    private PlaceSuggestService placeSuggestService;
    
    @InjectMocks
    private PlaceService placeService;

    @Test
    @DisplayName("키워드 검색이 정상적으로 위임되는지 확인")
    void searchByKeyword_DelegatesCorrectly() {
        // Given
        SearchRequest request = SearchRequest.builder()
            .query("카페")
            .lng(127.0)
            .lat(37.5)
            .radius(1000)
            .page(1)
            .size(10)
            .build();
            
        PageTemplate<PlaceItem> expectedResult = createSamplePageTemplate();
        
        given(placeSearchService.searchByKeyword(any(SearchRequest.class)))
            .willReturn(expectedResult);

        // When
        PageTemplate<PlaceItem> result = placeService.searchByKeyword(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResult);
        verify(placeSearchService).searchByKeyword(request);
    }

    @Test
    @DisplayName("카테고리 검색이 정상적으로 위임되는지 확인")
    void searchByCategory_DelegatesCorrectly() {
        // Given
        NearbyRequest request = NearbyRequest.builder()
            .category("PSY")
            .lng(127.0)
            .lat(37.5)
            .radius(1000)
            .page(1)
            .size(10)
            .build();
            
        PageTemplate<PlaceItem> expectedResult = createSamplePageTemplate();
        
        given(placeSearchService.searchByCategory(any(NearbyRequest.class)))
            .willReturn(expectedResult);

        // When
        PageTemplate<PlaceItem> result = placeService.searchByCategory(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResult);
        verify(placeSearchService).searchByCategory(request);
    }

    @Test
    @DisplayName("장소 상세 조회가 정상적으로 위임되는지 확인")
    void getPlaceDetail_DelegatesCorrectly() {
        // Given
        String placeId = "kakao:123456";
        PlaceDetail expectedDetail = createSamplePlaceDetail();
        
        given(placeDetailService.getPlaceDetail(anyString()))
            .willReturn(expectedDetail);

        // When
        PlaceDetail result = placeService.getPlaceDetail(placeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDetail);
        verify(placeDetailService).getPlaceDetail(placeId);
    }

    @Test
    @DisplayName("자동완성 제안이 정상적으로 위임되는지 확인")
    void suggest_DelegatesCorrectly() {
        // Given
        SuggestRequest request = SuggestRequest.builder()
            .query("강남")
            .lat(37.5)
            .lng(127.0)
            .build();
            
        SuggestResponse expectedResponse = createSampleSuggestResponse();
        
        given(placeSuggestService.suggest(any(SuggestRequest.class)))
            .willReturn(expectedResponse);

        // When
        SuggestResponse result = placeService.suggest(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        verify(placeSuggestService).suggest(request);
    }


    private PageTemplate<PlaceItem> createSamplePageTemplate() {
        Category category = Category.builder()
            .code("CAFE")
            .label("카페")
            .build();
            
        Address address = Address.builder()
            .road("서울 강남구 테헤란로 123")
            .lot("서울 강남구 역삼동 123-45")
            .build();
            
        PlaceItem item = PlaceItem.builder()
            .id("kakao:123456")
            .name("테스트 카페")
            .category(category)
            .address(address)
            .build();
            
        return new PageTemplate<>(1L, 1, 1, 10, false, List.of(item));
    }

    private PlaceDetail createSamplePlaceDetail() {
        Category category = Category.builder()
            .code("HP8")
            .label("병원")
            .build();
            
        Address address = Address.builder()
            .road("서울 강남구 테헤란로 123")
            .lot("서울 강남구 역삼동 123-45")
            .build();
            
        Location location = Location.builder()
            .lat(37.5012)
            .lng(127.0396)
            .build();
            
        return PlaceDetail.builder()
            .id("kakao:123456")
            .name("테스트 병원")
            .category(category)
            .address(address)
            .location(location)
            .phone("02-1234-5678")
            .build();
    }

    private SuggestResponse createSampleSuggestResponse() {
        SuggestItem item = SuggestItem.builder()
            .id("kakao:111")
            .name("강남역")
            .category("지하철역")
            .address("서울 강남구 강남대로")
            .build();
            
        return SuggestResponse.builder()
            .query("강남")
            .suggestions(List.of(item))
            .count(1)
            .locationBased(true)
            .responseTime(50)
            .build();
    }
}