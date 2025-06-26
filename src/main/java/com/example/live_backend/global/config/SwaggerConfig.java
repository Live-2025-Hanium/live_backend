package com.example.live_backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.StringSchema;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    private static final String API_TITLE = "Lively API";
    private static final String API_VERSION = "v1.0";
    private static final String API_DESCRIPTION = "라이블리 API 문서";

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        // 페이지네이션 파라미터 설정
        Parameter page = new Parameter()
                .in("query")
                .name("page")
                .description("페이지 번호 0부터 시작")
                .required(false)
                .schema(new StringSchema().example("0"));

        Parameter size = new Parameter()
                .in("query")
                .name("size")
                .description("한 페이지에 들어갈 항목들의 갯수")
                .required(false)
                .schema(new StringSchema().example("10"));

        return new OpenAPI()
                .servers(Arrays.asList(
                        new Server().url("http://localhost:" + serverPort).description("Local 서버")
                        ,new Server().url("https://live-hanium.app").description("Lively API 서버")
                ))
                .info(new Info()
                        .title(API_TITLE)
                        .version(API_VERSION)
                        .description(API_DESCRIPTION)
                        .termsOfService("https://www.example.com/terms")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact()
                                .name("Lively Team")
                                .url("https://www.example.com/support")
                                .email("live.2025hanium@gmail.com"))
                )
                .components(
                    new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuthScheme)
                        .addParameters("page", page)
                        .addParameters("size", size)
                )
                .addSecurityItem(securityRequirement);
    }
}
