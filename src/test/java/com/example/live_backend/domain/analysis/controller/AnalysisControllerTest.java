package com.example.live_backend.domain.analysis.controller;

import com.example.live_backend.domain.analysis.controller.controller.AnalysisController;
import com.example.live_backend.domain.analysis.controller.dto.MonthlyParticipationResponseDto;
import com.example.live_backend.domain.analysis.controller.service.AnalysisService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisController 테스트")
class AnalysisControllerTest {

    @InjectMocks
    private AnalysisController analysisController;

    @Mock
    private AnalysisService analysisService;

    @Test
    @DisplayName("월별 미션 완료율 조회 - 성공 및 서비스 위임 확인")
    void getParticipation_ReturnsSuccessAndDelegatesToService() {

        // Given
        Long memberId = 1L;
        PrincipalDetails user = new PrincipalDetails(memberId, "oauth-1", "USER", "닉네임", "user@example.com");
        YearMonth expectedYm = YearMonth.now();

        MonthlyParticipationResponseDto dto = MonthlyParticipationResponseDto.from(expectedYm, 10L, 7L, 70.0);
        given(analysisService.getMonthlyParticipation(eq(memberId), any(YearMonth.class))).willReturn(dto);

        // When
        ResponseHandler<MonthlyParticipationResponseDto> response = analysisController.getParticipation(user);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);

        ArgumentCaptor<YearMonth> ymCaptor = ArgumentCaptor.forClass(YearMonth.class);
        verify(analysisService).getMonthlyParticipation(eq(memberId), ymCaptor.capture());
        assertThat(ymCaptor.getValue()).isEqualTo(expectedYm);
    }
}