package com.example.live_backend.domain.place.controller.docs;

import com.example.live_backend.domain.place.dto.ActiveMissionPlace;
import com.example.live_backend.domain.place.dto.PlaceDetail;
import com.example.live_backend.domain.place.dto.PlaceItem;
import com.example.live_backend.domain.place.dto.SuggestResponse;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.domain.place.dto.request.SuggestRequest;
import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.global.error.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/v1/places")
@Tag(name = "Places", description = "장소 검색 및 조회 API")
public interface PlacesApiDocs {
    
    @Operation(
        summary = "자동완성 제안",
        description = "입력 중인 검색어에 대한 자동완성 제안을 반환합니다. 실제 검색이 아닌 입력 보조 기능입니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "제안 목록 반환 성공",
            content = @Content(schema = @Schema(implementation = SuggestResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "레이트 리밋 초과",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        )
    })
    @GetMapping("/suggest")
    ResponseHandler<SuggestResponse> suggest(
        @Parameter(description = "자동완성 요청 파라미터") @Valid @ModelAttribute SuggestRequest request
    );
    
    @Operation(
        summary = "키워드로 장소 검색",
        description = "카카오 Local API를 통해 키워드로 주변 장소를 검색합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @Content(schema = @Schema(implementation = PageTemplate.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "레이트 리밋 초과",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        ),
        @ApiResponse(
            responseCode = "502",
            description = "카카오 API 오류",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        )
    })
    @GetMapping("/search")
    ResponseHandler<PageTemplate<PlaceItem>> searchByKeyword(
        @Parameter(description = "검색 요청 파라미터") @Valid @ModelAttribute SearchRequest request
    );
    
    @Operation(
        summary = "카테고리로 주변 장소 검색",
        description = "카테고리를 기반으로 주변 장소를 검색합니다. (LEI: 여가시설, PSY: 정신건강의학과, WEL: 복지시설, CSC: 상담센터)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @Content(schema = @Schema(implementation = PageTemplate.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "레이트 리밋 초과",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        ),
        @ApiResponse(
            responseCode = "502",
            description = "카카오 API 오류",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        )
    })
    @GetMapping("/nearby")
    ResponseHandler<PageTemplate<PlaceItem>> searchByCategory(
        @Parameter(description = "카테고리 검색 요청 파라미터") @Valid @ModelAttribute NearbyRequest request
    );
    
    @Operation(
        summary = "장소 상세 정보 조회",
        description = "장소 ID를 통해 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PlaceDetail.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "장소를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "레이트 리밋 초과",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        ),
        @ApiResponse(
            responseCode = "502",
            description = "카카오 API 오류",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        )
    })
    @GetMapping("/{placeId}")
    ResponseHandler<PlaceDetail> getPlaceDetail(
        @Parameter(description = "장소 ID (형식: kakao:123456789)", example = "kakao:123456789") 
        @PathVariable String placeId
    );
    
    @Operation(
        summary = "활성 미션 장소 조회",
        description = "현재 사용자의 활성화된 방문 미션 장소 정보를 조회합니다. (JWT 인증 필요)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "활성 미션 존재",
            content = @Content(schema = @Schema(implementation = ActiveMissionPlace.class))
        ),
        @ApiResponse(
            responseCode = "204",
            description = "활성 미션 없음"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ResponseHandler.class))
        )
    })
    @GetMapping("/me/missions/active")
    ResponseHandler<ActiveMissionPlace> getActiveMissionPlace(
        @Parameter(hidden = true) UserDetails userDetails
    );
}