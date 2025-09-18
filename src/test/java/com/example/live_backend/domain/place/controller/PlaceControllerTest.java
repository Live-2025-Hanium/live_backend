package com.example.live_backend.domain.place.controller;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.domain.place.dto.request.*;
import com.example.live_backend.domain.place.service.PlaceMissionService;
import com.example.live_backend.domain.place.service.PlaceService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.global.security.PrincipalDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceController 테스트")
class PlaceControllerTest {

    @Mock
    private PlaceService placeService;
    
    @Mock
    private PlaceMissionService placeMissionService;

    @InjectMocks
    private PlaceController placeController;

    private static final String TEST_OAUTH_ID = "oauth-12345";
    private static final Long TEST_USER_ID = 1L;

    @Nested
    @DisplayName("자동완성 제안 API")
    class SuggestTest {

        @Test
        @DisplayName("성공 - 정상적인 키워드로 자동완성 결과 반환")
        void suggest_Success() {
            // Given
            SuggestRequest request = createSuggestRequest("강남");
            SuggestResponse response = createSuggestResponse();
            
            given(placeService.suggest(any(SuggestRequest.class)))
                .willReturn(response);

            // When
            ResponseHandler<SuggestResponse> result = placeController.suggest(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getSuggestions()).hasSize(3);
            assertThat(result.getData().getSuggestions().get(0).getName()).contains("강남역");
            verify(placeService).suggest(request);
        }

        @Test
        @DisplayName("성공 - 빈 키워드로 빈 결과 반환")
        void suggest_EmptyQuery_ReturnsEmptyResult() {
            // Given
            SuggestRequest request = createSuggestRequest("");
            SuggestResponse emptyResponse = SuggestResponse.empty("");
            
            given(placeService.suggest(any(SuggestRequest.class)))
                .willReturn(emptyResponse);

            // When
            ResponseHandler<SuggestResponse> result = placeController.suggest(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getSuggestions()).isEmpty();
        }

        @Test
        @DisplayName("실패 - 외부 API 오류")
        void suggest_ExternalApiError() {
            // Given
            SuggestRequest request = createSuggestRequest("강남");
            
            given(placeService.suggest(any(SuggestRequest.class)))
                .willThrow(new CustomException(ErrorCode.EXTERNAL_API_ERROR));

            // When & Then
            assertThatThrownBy(() -> placeController.suggest(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Nested
    @DisplayName("키워드 검색 API")
    class SearchByKeywordTest {

        @Test
        @DisplayName("성공 - 키워드로 장소 검색")
        void searchByKeyword_Success() {
            // Given
            SearchRequest request = createSearchRequest("카페", 127.0, 37.5);
            PageTemplate<PlaceItem> response = createPageTemplate();
            
            given(placeService.searchByKeyword(any(SearchRequest.class)))
                .willReturn(response);

            // When
            ResponseHandler<PageTemplate<PlaceItem>> result = placeController.searchByKeyword(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().content()).hasSize(2);
            assertThat(result.getData().totalElements()).isEqualTo(100L);
            assertThat(result.getData().currentPage()).isEqualTo(1);
            verify(placeService).searchByKeyword(request);
        }

        @Test
        @DisplayName("성공 - 검색 결과 없음")
        void searchByKeyword_NoResults() {
            // Given
            SearchRequest request = createSearchRequest("존재하지않는장소", 127.0, 37.5);
            PageTemplate<PlaceItem> emptyResponse = createEmptyPageTemplate();
            
            given(placeService.searchByKeyword(any(SearchRequest.class)))
                .willReturn(emptyResponse);

            // When
            ResponseHandler<PageTemplate<PlaceItem>> result = placeController.searchByKeyword(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().content()).isEmpty();
            assertThat(result.getData().totalElements()).isEqualTo(0L);
        }

        @Test
        @DisplayName("실패 - API 타임아웃")
        void searchByKeyword_Timeout() {
            // Given
            SearchRequest request = createSearchRequest("카페", 127.0, 37.5);
            
            given(placeService.searchByKeyword(any(SearchRequest.class)))
                .willThrow(new CustomException(ErrorCode.EXTERNAL_API_TIMEOUT));

            // When & Then
            assertThatThrownBy(() -> placeController.searchByKeyword(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_TIMEOUT);
        }
    }

    @Nested
    @DisplayName("카테고리별 주변 검색 API")
    class SearchByCategoryTest {

        @Test
        @DisplayName("성공 - 카테고리로 주변 장소 검색")
        void searchByCategory_Success() {
            // Given
            NearbyRequest request = createNearbyRequest("HOSPITAL", 127.0, 37.5);
            PageTemplate<PlaceItem> response = createPageTemplate();
            
            given(placeService.searchByCategory(any(NearbyRequest.class)))
                .willReturn(response);

            // When
            ResponseHandler<PageTemplate<PlaceItem>> result = placeController.searchByCategory(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().content()).hasSize(2);
            verify(placeService).searchByCategory(request);
        }

        @Test
        @DisplayName("실패 - 잘못된 카테고리")
        void searchByCategory_InvalidCategory() {
            // Given
            NearbyRequest request = createNearbyRequest("INVALID_CATEGORY", 127.0, 37.5);
            
            given(placeService.searchByCategory(any(NearbyRequest.class)))
                .willThrow(new CustomException(ErrorCode.INVALID_CATEGORY));

            // When & Then
            assertThatThrownBy(() -> placeController.searchByCategory(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CATEGORY);
        }

        @Test
        @DisplayName("성공 - 좌표 없이 기본값으로 검색")
        void searchByCategory_WithDefaultLocation() {
            // Given
            NearbyRequest request = createNearbyRequest("HOSPITAL", null, null);
            PageTemplate<PlaceItem> response = createPageTemplate();
            
            given(placeService.searchByCategory(any(NearbyRequest.class)))
                .willReturn(response);

            // When
            ResponseHandler<PageTemplate<PlaceItem>> result = placeController.searchByCategory(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }
    }

    @Nested
    @DisplayName("장소 상세 조회 API")
    class GetPlaceDetailTest {

        @Test
        @DisplayName("성공 - 장소 상세 정보 조회")
        void getPlaceDetail_Success() {
            // Given
            String placeId = "kakao:123456";
            PlaceDetail detail = createPlaceDetail(placeId);
            
            given(placeService.getPlaceDetail(eq(placeId)))
                .willReturn(detail);

            // When
            ResponseHandler<PlaceDetail> result = placeController.getPlaceDetail(placeId);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(placeId);
            assertThat(result.getData().getName()).isEqualTo("테스트 병원");
            verify(placeService).getPlaceDetail(placeId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 장소")
        void getPlaceDetail_NotFound() {
            // Given
            String placeId = "kakao:999999";
            
            given(placeService.getPlaceDetail(eq(placeId)))
                .willThrow(new CustomException(ErrorCode.PLACE_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> placeController.getPlaceDetail(placeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 잘못된 장소 ID 형식")
        void getPlaceDetail_InvalidFormat() {
            // Given
            String placeId = "invalid-format";
            
            given(placeService.getPlaceDetail(eq(placeId)))
                .willThrow(new CustomException(ErrorCode.INVALID_VALUE));

            // When & Then
            assertThatThrownBy(() -> placeController.getPlaceDetail(placeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VALUE);
        }
    }

    @Nested
    @DisplayName("활성 미션 장소 조회 API")
    class GetActiveMissionPlaceTest {

        @Test
        @DisplayName("성공 - 활성 미션이 있는 경우")
        void getActiveMissionPlace_WithActiveMission() {
            // Given
            PrincipalDetails userDetails = createPrincipalDetails();
            ActiveMissionPlace activeMission = createActiveMissionPlace();
            
            given(placeMissionService.getActiveMissionPlace(eq(TEST_OAUTH_ID)))
                .willReturn(Optional.of(activeMission));

            // When
            ResponseHandler<ActiveMissionPlace> result = placeController.getActiveMissionPlace(userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getPlaceId()).isEqualTo("kakao:789012");
            assertThat(result.getData().getName()).isEqualTo("미션 장소");
        }

        @Test
        @DisplayName("성공 - 활성 미션이 없는 경우")
        void getActiveMissionPlace_NoActiveMission() {
            // Given
            PrincipalDetails userDetails = createPrincipalDetails();
            
            given(placeMissionService.getActiveMissionPlace(eq(TEST_OAUTH_ID)))
                .willReturn(Optional.empty());

            // When
            ResponseHandler<ActiveMissionPlace> result = placeController.getActiveMissionPlace(userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("실패 - 사용자를 찾을 수 없음")
        void getActiveMissionPlace_UserNotFound() {
            // Given
            PrincipalDetails userDetails = createPrincipalDetails();
            
            given(placeMissionService.getActiveMissionPlace(eq(TEST_OAUTH_ID)))
                .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> placeController.getActiveMissionPlace(userDetails))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }


    private PrincipalDetails createPrincipalDetails() {
        return new PrincipalDetails(TEST_USER_ID, TEST_OAUTH_ID, "USER", "테스트유저", "test@example.com");
    }

    private SuggestRequest createSuggestRequest(String query) {
        return SuggestRequest.builder()
            .query(query)
            .lat(37.5)
            .lng(127.0)
            .build();
    }

    private SuggestResponse createSuggestResponse() {
        List<SuggestItem> items = List.of(
            SuggestItem.builder()
                .id("kakao:111")
                .name("강남역 12번 출구")
                .address("서울 강남구 강남대로 396")
                .category("지하철역")
                .build(),
            SuggestItem.builder()
                .id("kakao:222")
                .name("강남역 CGV")
                .address("서울 강남구 강남대로 438")
                .category("영화관")
                .build(),
            SuggestItem.builder()
                .id("kakao:333")
                .name("강남 교보문고")
                .address("서울 강남구 강남대로 465")
                .category("서점")
                .build()
        );
        return SuggestResponse.builder()
            .query("강남")
            .suggestions(items)
            .count(3)
            .locationBased(true)
            .responseTime(50)
            .build();
    }

    private SearchRequest createSearchRequest(String query, Double lng, Double lat) {
        return SearchRequest.builder()
            .query(query)
            .lng(lng)
            .lat(lat)
            .radius(1000)
            .page(1)
            .size(10)
            .build();
    }

    private NearbyRequest createNearbyRequest(String category, Double lng, Double lat) {
        return NearbyRequest.builder()
            .category(category)
            .lng(lng != null ? lng : 127.0)
            .lat(lat != null ? lat : 37.5)
            .radius(1000)
            .page(1)
            .size(10)
            .build();
    }

    private PageTemplate<PlaceItem> createPageTemplate() {
        Category cafe = Category.builder().code("CAFE").label("카페").build();
        Address address1 = Address.builder().road("서울 강남구 강남대로 390").build();
        Address address2 = Address.builder().road("서울 강남구 강남대로 394").build();
        
        List<PlaceItem> items = List.of(
            PlaceItem.builder()
                .id("kakao:111111")
                .name("스타벅스 강남점")
                .category(cafe)
                .address(address1)
                .build(),
            PlaceItem.builder()
                .id("kakao:222222")
                .name("투썸플레이스 강남역점")
                .category(cafe)
                .address(address2)
                .build()
        );
        return new PageTemplate<>(100L, 10, 1, 10, true, items);
    }

    private PageTemplate<PlaceItem> createEmptyPageTemplate() {
        return new PageTemplate<>(0L, 0, 1, 10, false, new ArrayList<>());
    }

    private PlaceDetail createPlaceDetail(String placeId) {
        return PlaceDetail.builder()
            .id(placeId)
            .name("테스트 병원")
            .category(Category.builder().code("HOS").label("병원").build())
            .address(Address.builder()
                .road("서울 강남구 테헤란로 123")
                .lot("서울 강남구 역삼동 123-45")
                .build())
            .phone("02-1234-5678")
            .location(Location.builder()
                .lat(37.5012)
                .lng(127.0396)
                .build())
            .build();
    }

    private ActiveMissionPlace createActiveMissionPlace() {
        return ActiveMissionPlace.builder()
            .placeId("kakao:789012")
            .name("미션 장소")
            .location(Location.builder()
                .lat(37.5665)
                .lng(126.9780)
                .build())
            .build();
    }
}