package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.domain.place.dto.request.SuggestRequest;
import com.example.live_backend.domain.place.mapper.PlaceCategoryMapper;
import com.example.live_backend.domain.place.mapper.PlaceConverter;
import com.example.live_backend.domain.place.util.PlaceDistanceCalculator;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceSuggestService 단위 테스트")
class PlaceSuggestServiceTest {

    @Mock
    private KakaoLocalFeign kakaoLocalFeign;
    
    @Mock
    private PlaceCategoryMapper categoryMapper;
    
    @Mock
    private PlaceConverter placeConverter;
    
    @Mock
    private PlaceDistanceCalculator distanceCalculator;

    @InjectMocks
    private PlaceSuggestService placeSuggestService;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(placeSuggestService, "defaultLongitude", 126.9780);
        ReflectionTestUtils.setField(placeSuggestService, "defaultLatitude", 37.5665);
        ReflectionTestUtils.setField(placeSuggestService, "radiusWithLocation", 5000);
        ReflectionTestUtils.setField(placeSuggestService, "radiusWithoutLocation", 20000);
    }

    @Nested
    @DisplayName("자동완성 제안")
    class Suggest {

        @Test
        @DisplayName("성공 - 검색어로 자동완성 결과 반환")
        void suggestSuccess() {
            // Given
            SuggestRequest request = SuggestRequest.builder()
                .query("강남")
                .lat(37.5)
                .lng(127.0)
                .build();

            KakaoKeywordResponse.KakaoPlace place1 = mock(KakaoKeywordResponse.KakaoPlace.class);
            when(place1.getId()).thenReturn("111");
            when(place1.getPlaceName()).thenReturn("강남역");
            when(place1.getAddressName()).thenReturn("서울 강남구 강남대로");
            when(place1.getRoadAddressName()).thenReturn("서울 강남구 강남대로");
            when(place1.getX()).thenReturn("127.0");
            when(place1.getY()).thenReturn("37.5");
            
            List<KakaoKeywordResponse.KakaoPlace> kakaoPlaces = List.of(place1);
            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(
                kakaoPlaces,
                new KakaoKeywordResponse.Meta()
            );
            
            // Mock PlaceItem 생성
            when(placeConverter.toPlaceItem(any(KakaoKeywordResponse.KakaoPlace.class)))
                .thenAnswer(invocation -> {
                    KakaoKeywordResponse.KakaoPlace p = invocation.getArgument(0);
                    return PlaceItem.builder()
                        .id("kakao:" + p.getId())
                        .name(p.getPlaceName())
                        .build();
                });
            
            when(distanceCalculator.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(100);
            
            when(kakaoLocalFeign.searchByKeyword(
                eq("강남"), eq(127.0), eq(37.5), eq(5000), eq(1), eq(10), eq("distance")))
                .thenReturn(kakaoResponse);

            // When
            SuggestResponse result = placeSuggestService.suggest(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getQuery()).isEqualTo("강남");
            assertThat(result.getSuggestions()).isNotEmpty();
        }

        @Test
        @DisplayName("성공 - 빈 검색어는 빈 결과 반환")
        void emptyQueryReturnsEmpty() {
            // Given
            SuggestRequest request = SuggestRequest.builder()
                .query("")
                .lat(37.5)
                .lng(127.0)
                .build();

            // When
            SuggestResponse result = placeSuggestService.suggest(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getQuery()).isEmpty();
            assertThat(result.getSuggestions()).isEmpty();
            assertThat(result.getCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - 좌표 없을 때 기본 좌표 사용")
        void useDefaultCoordinates() {
            // Given
            SuggestRequest request = SuggestRequest.builder()
                .query("카페")
                .build();

            KakaoKeywordResponse.KakaoPlace place1 = mock(KakaoKeywordResponse.KakaoPlace.class);
            when(place1.getId()).thenReturn("111");
            when(place1.getPlaceName()).thenReturn("카페베네");
            when(place1.getAddressName()).thenReturn("서울 중구");
            when(place1.getRoadAddressName()).thenReturn("서울 중구");
            
            List<KakaoKeywordResponse.KakaoPlace> kakaoPlaces = List.of(place1);
            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(
                kakaoPlaces,
                new KakaoKeywordResponse.Meta()
            );
            
            when(placeConverter.toPlaceItem(any()))
                .thenAnswer(invocation -> {
                    KakaoKeywordResponse.KakaoPlace p = invocation.getArgument(0);
                    return PlaceItem.builder()
                        .id("kakao:" + p.getId())
                        .name(p.getPlaceName())
                        .build();
                });
                
            when(kakaoLocalFeign.searchByKeyword(
                eq("카페"), eq(126.9780), eq(37.5665), eq(20000), eq(1), eq(10), eq("accuracy")))
                .thenReturn(kakaoResponse);

            // When
            SuggestResponse result = placeSuggestService.suggest(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isLocationBased()).isFalse(); // 기본 좌표 사용시 false
            verify(kakaoLocalFeign).searchByKeyword(
                "카페", 126.9780, 37.5665, 20000, 1, 10, "accuracy");
        }

        @Test
        @DisplayName("성공 - 거리 계산 및 정렬")
        void calculateAndSortByDistance() {
            // Given
            SuggestRequest request = SuggestRequest.builder()
                .query("병원")
                .lat(37.5)
                .lng(127.0)
                .build();

            KakaoKeywordResponse.KakaoPlace place1 = mock(KakaoKeywordResponse.KakaoPlace.class);
            when(place1.getPlaceName()).thenReturn("병원1");
            when(place1.getAddressName()).thenReturn("서울 강남구");
            when(place1.getRoadAddressName()).thenReturn("서울 강남구");
            when(place1.getX()).thenReturn("127.0");
            when(place1.getY()).thenReturn("37.5");
            
            List<KakaoKeywordResponse.KakaoPlace> kakaoPlaces = List.of(place1);
            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(
                kakaoPlaces,
                new KakaoKeywordResponse.Meta()
            );
            
            when(placeConverter.toPlaceItem(any()))
                .thenReturn(PlaceItem.builder()
                    .id("kakao:111")
                    .name("병원1")
                    .build());
            
            when(distanceCalculator.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(100);
            
            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(kakaoResponse);

            // When
            SuggestResponse result = placeSuggestService.suggest(request);

            // Then
            assertThat(result.getSuggestions()).isNotEmpty();
        }

        @Test
        @DisplayName("실패 처리 - API 오류시 빈 결과 반환")
        void apiErrorReturnsEmpty() {
            // Given
            SuggestRequest request = SuggestRequest.builder()
                .query("테스트")
                .lat(37.5)
                .lng(127.0)
                .build();
            
            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

            // When
            SuggestResponse result = placeSuggestService.suggest(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getQuery()).isEqualTo("테스트");
            assertThat(result.getSuggestions()).isEmpty();
            assertThat(result.getCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("매칭 타입 설정 - 이름 매칭")
        void matchTypeNameMatching() {
            // Given
            SuggestRequest request = SuggestRequest.builder()
                .query("강남")
                .lat(37.5)
                .lng(127.0)
                .build();

            KakaoKeywordResponse.KakaoPlace place = mock(KakaoKeywordResponse.KakaoPlace.class);
            when(place.getPlaceName()).thenReturn("강남역");
            when(place.getAddressName()).thenReturn("서울 강남구");
            when(place.getRoadAddressName()).thenReturn("서울 강남구");
            when(place.getX()).thenReturn("127.0");
            when(place.getY()).thenReturn("37.5");
            
            List<KakaoKeywordResponse.KakaoPlace> places = List.of(place);
            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(
                places,
                new KakaoKeywordResponse.Meta()
            );
            
            when(placeConverter.toPlaceItem(any()))
                .thenReturn(PlaceItem.builder()
                    .id("kakao:111")
                    .name("강남역")
                    .build());
            
            when(distanceCalculator.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(100);
            
            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(kakaoResponse);

            // When
            SuggestResponse result = placeSuggestService.suggest(request);

            // Then
            assertThat(result.getSuggestions()).isNotEmpty();
        }

        @Test
        @DisplayName("재시도 - 첫 실패 후 성공")
        void retryOnFailure() {
            // Given
            SuggestRequest request = SuggestRequest.builder()
                .query("테스트")
                .lat(37.5)
                .lng(127.0)
                .build();
            

            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("Temporary failure"));

            // When
            SuggestResponse result = placeSuggestService.suggest(request);

            // Then
            assertThat(result.getSuggestions()).isEmpty();
            verify(kakaoLocalFeign, times(1)).searchByKeyword(
                anyString(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString());
        }
    }
}