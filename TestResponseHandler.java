import com.example.live_backend.global.error.response.ResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestResponseHandler {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // 성공 응답 테스트
        ResponseHandler<String> successResponse = ResponseHandler.success("테스트 데이터");
        String successJson = mapper.writeValueAsString(successResponse);
        System.out.println("=== 성공 응답 ===");
        System.out.println(successJson);
        
        // 에러 응답 테스트  
        ResponseHandler<Object> errorResponse = ResponseHandler.error("INVALID_INPUT", "잘못된 입력입니다");
        String errorJson = mapper.writeValueAsString(errorResponse);
        System.out.println("
=== 에러 응답 ===");
        System.out.println(errorJson);
    }
}
