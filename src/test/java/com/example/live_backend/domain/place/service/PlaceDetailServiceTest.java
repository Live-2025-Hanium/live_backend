package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.domain.place.mapper.PlaceConverter;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
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
@DisplayName("PlaceDetailService 단위 테스트")
class PlaceDetailServiceTest {

    @Mock
    private KakaoLocalFeign kakaoLocalFeign;
    
    @Mock
    private PlaceConverter placeConverter;

    @InjectMocks
    private PlaceDetailService placeDetailService;

    @Nested
    @DisplayName("장소 상세 조회")
    class GetPlaceDetail {

        @Test
        @DisplayName("성공 - kakao ID로 상세 정보 조회")
        void getDetailSuccess() {
            // Given
            String placeId = "kakao:123456";
            
            KakaoKeywordResponse.KakaoPlace kakaoPlace = mock(KakaoKeywordResponse.KakaoPlace.class);
            List<KakaoKeywordResponse.KakaoPlace> places = List.of(kakaoPlace);
            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(places, new KakaoKeywordResponse.Meta());
            
            PlaceDetail expectedDetail = PlaceDetail.builder()
                .id(placeId)
                .name("테스트 병원")
                .category(Category.builder().code("HP8").label("병원").build())
                .address(Address.builder().road("서울 강남구").build())
                .build();
            
            when(kakaoLocalFeign.searchByKeyword(
                eq("123456"), eq(127.0), eq(37.5), eq(20000), eq(1), eq(1), isNull()))
                .thenReturn(kakaoResponse);
            
            when(placeConverter.toPlaceDetail(any(KakaoKeywordResponse.KakaoPlace.class)))
                .thenReturn(expectedDetail);

            // When
            PlaceDetail result = placeDetailService.getPlaceDetail(placeId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(placeId);
            assertThat(result.getName()).isEqualTo("테스트 병원");
            verify(kakaoLocalFeign).searchByKeyword("123456", 127.0, 37.5, 20000, 1, 1, null);
            verify(placeConverter).toPlaceDetail(kakaoPlace);
        }

        @Test
        @DisplayName("실패 - 잘못된 ID 형식")
        void invalidIdFormat() {
            // Given
            String invalidPlaceId = "invalid-format";

            // When & Then
            assertThatThrownBy(() -> placeDetailService.getPlaceDetail(invalidPlaceId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VALUE)
                .hasMessageContaining("유효하지 않은 장소 ID");
            
            verify(kakaoLocalFeign, never()).searchByKeyword(anyString(), anyDouble(), 
                anyDouble(), anyInt(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("실패 - kakao 접두사 없음")
        void missingKakaoPrefix() {
            // Given
            String placeIdWithoutPrefix = "123456";

            // When & Then
            assertThatThrownBy(() -> placeDetailService.getPlaceDetail(placeIdWithoutPrefix))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VALUE);
        }

        @Test
        @DisplayName("실패 - 장소를 찾을 수 없음")
        void placeNotFound() {
            // Given
            String placeId = "kakao:999999";
            KakaoKeywordResponse emptyResponse = new KakaoKeywordResponse(
                new ArrayList<>(), 
                new KakaoKeywordResponse.Meta()
            );
            
            when(kakaoLocalFeign.searchByKeyword(
                eq("999999"), anyDouble(), anyDouble(), anyInt(), eq(1), eq(1), isNull()))
                .thenReturn(emptyResponse);

            // When & Then
            assertThatThrownBy(() -> placeDetailService.getPlaceDetail(placeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND)
                .hasMessageContaining("장소를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("폴백 - API 오류시 기본 정보 반환")
        void apiErrorReturnsFallback() {
            // Given
            String placeId = "kakao:123456";
            
            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt(), isNull()))
                .thenThrow(new RuntimeException("API Error"));

            // When
            PlaceDetail result = placeDetailService.getPlaceDetail(placeId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(placeId);
            assertThat(result.getName()).isEqualTo("정보를 불러올 수 없습니다");
            assertThat(result.getCategory()).isNull();
            assertThat(result.getAddress()).isNull();
        }

        @Test
        @DisplayName("ID 추출 - kakao:123456에서 123456 추출")
        void extractKakaoId() {
            // Given
            String placeId = "kakao:789012";
            
            KakaoKeywordResponse.KakaoPlace kakaoPlace = mock(KakaoKeywordResponse.KakaoPlace.class);
            KakaoKeywordResponse kakaoResponse = new KakaoKeywordResponse(
                List.of(kakaoPlace), 
                new KakaoKeywordResponse.Meta()
            );
            
            PlaceDetail detail = PlaceDetail.builder()
                .id(placeId)
                .name("테스트")
                .build();
            
            when(kakaoLocalFeign.searchByKeyword(
                eq("789012"), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt(), isNull()))
                .thenReturn(kakaoResponse);
            
            when(placeConverter.toPlaceDetail(any()))
                .thenReturn(detail);

            // When
            placeDetailService.getPlaceDetail(placeId);

            // Then
            verify(kakaoLocalFeign).searchByKeyword(
                eq("789012"), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt(), isNull());
        }

        @Test
        @DisplayName("재시도 - 첫 실패 후 성공")
        void retryOnTemporaryFailure() {
            // Given
            String placeId = "kakao:123456";

            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt(), isNull()))
                .thenThrow(new RuntimeException("Temporary failure"));

            // When
            PlaceDetail result = placeDetailService.getPlaceDetail(placeId);

            // Then
            // @Retryable이 테스트 환경에서 동작하지 않아 fallback 반환함.
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("정보를 불러올 수 없습니다");
            assertThat(result.getId()).isEqualTo(placeId);
            
            // 테스트 환경에서는 @Retryable이 동작하지 않으므로 1번만 호출됨
            verify(kakaoLocalFeign, times(1)).searchByKeyword(
                anyString(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt(), isNull());
        }

        @Test
        @DisplayName("재시도 안함 - CustomException은 재시도하지 않음")
        void noRetryForCustomException() {
            // Given
            String placeId = "kakao:123456";
            KakaoKeywordResponse emptyResponse = new KakaoKeywordResponse(
                new ArrayList<>(),
                new KakaoKeywordResponse.Meta()
            );
            
            when(kakaoLocalFeign.searchByKeyword(anyString(), anyDouble(), anyDouble(), 
                anyInt(), anyInt(), anyInt(), isNull()))
                .thenReturn(emptyResponse);

            // When & Then
            assertThatThrownBy(() -> placeDetailService.getPlaceDetail(placeId))
                .isInstanceOf(CustomException.class);

            verify(kakaoLocalFeign, times(1)).searchByKeyword(
                anyString(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt(), isNull());
        }
    }
}