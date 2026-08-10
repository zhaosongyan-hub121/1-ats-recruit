package com.recruit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("招聘管理后端 API")
                        .version("v1.0")
                        .description("候选人/职位管理、规则化筛选与全文检索的 REST API 文档")
                        .contact(new Contact().name("Recruit Backend").email("dev@recruit.com")));
    }
}
