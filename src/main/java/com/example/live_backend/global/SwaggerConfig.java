package com.example.live_backend.global;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        Info info = new Info()
                .version("v1.0") //버전
                .title("Lively API") //이름
                .description("라이블리 API"); //설명
        return new OpenAPI()
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Local 서버")
                        //new Server().url("").description("Production 서버")
                ))
                .info(info);
    }

}
