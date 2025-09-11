package com.example.live_backend.domain.place.exception;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

public class PlaceException extends CustomException {
    
    public PlaceException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public PlaceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    public static PlaceException placeNotFound(String placeId) {
        return new PlaceException(ErrorCode.PLACE_NOT_FOUND, "장소를 찾을 수 없습니다: " + placeId);
    }
    
    public static PlaceException invalidPlaceId(String placeId) {
        return new PlaceException(ErrorCode.INVALID_VALUE, "잘못된 장소 ID 형식입니다: " + placeId);
    }
    
    public static PlaceException invalidCategory(String category) {
        return new PlaceException(ErrorCode.INVALID_VALUE, "지원하지 않는 카테고리입니다: " + category);
    }
    
    public static PlaceException kakaoApiError(String message) {
        return new PlaceException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API 오류: " + message);
    }
    
    public static PlaceException searchFailed(String query) {
        return new PlaceException(ErrorCode.EXTERNAL_API_ERROR, "장소 검색에 실패했습니다: " + query);
    }
    
    public static PlaceException userNotFound(String oauthId) {
        return new PlaceException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + oauthId);
    }
}