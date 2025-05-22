package com.example.live_backend.example.controller;

import com.example.live_backend.example.dto.request.ExampleRequestDto;
import com.example.live_backend.example.dto.response.ExampleResponseDto;
import com.example.live_backend.example.service.ExampleService;
import com.example.live_backend.common.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
@Tag(name = "Example API", description = "Swagger 테스트를 위한 예제 API")
public class ExampleController {

    private final ExampleService exampleService;

    @Operation(
        summary = "예제 조회", 
        description = "ID로 예제 데이터를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "예제 조회 성공"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHandler<ExampleResponseDto>> getExample(
            @Parameter(description = "예제 ID", required = true, example = "1") 
            @PathVariable Long id) {
        return ResponseEntity.ok(ResponseHandler.response(exampleService.getExampleById(id)));
    }

    @Operation(
        summary = "예제 목록 조회", 
        description = "모든 예제 데이터 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "예제 목록 조회 성공"
        )
    })
    @GetMapping
    public ResponseEntity<ResponseHandler<List<ExampleResponseDto>>> getAllExamples() {
        return ResponseEntity.ok(ResponseHandler.response(exampleService.getAllExamples()));
    }

    @Operation(
        summary = "예제 생성", 
        description = "새로운 예제 데이터를 생성합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "예제 생성 성공"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청"
        )
    })
    @PostMapping
    public ResponseEntity<ResponseHandler<ExampleResponseDto>> createExample(
            @Parameter(description = "생성할 예제 정보", required = true) 
            @RequestBody ExampleRequestDto request) {
        return ResponseEntity.ok(ResponseHandler.response(exampleService.createExample(request)));
    }

    @Operation(
        summary = "예제 수정", 
        description = "기존 예제 데이터를 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "예제 수정 성공"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseHandler<ExampleResponseDto>> updateExample(
            @Parameter(description = "수정할 예제 ID", required = true, example = "1") 
            @PathVariable Long id,
            @Parameter(description = "수정할 예제 정보", required = true) 
            @RequestBody ExampleRequestDto request) {
        return ResponseEntity.ok(ResponseHandler.response(exampleService.updateExample(id, request)));
    }

    @Operation(
        summary = "예제 삭제", 
        description = "예제 데이터를 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "예제 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseHandler<String>> deleteExample(
            @Parameter(description = "삭제할 예제 ID", required = true, example = "1") 
            @PathVariable Long id) {
        exampleService.deleteExample(id);
        return ResponseEntity.ok(ResponseHandler.response("삭제 완료"));
    }
} 