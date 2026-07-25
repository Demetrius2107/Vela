package com.vela.im.service.interfaces.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * <p>Title: Knife4jConfig</p>
 * <p>Description: Knife4j/Swagger 接口文档配置，访问地址：<a href="http://localhost:8000/doc.html">...</a></p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-07-25
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Bean
    public Docket defaultApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.vela.im.service"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Vela IM 接口文档")
                .description("基于 DDD 六边形架构的即时通讯系统 REST API")
                .contact(new Contact("wanqiu", "", ""))
                .version("1.0.0")
                .build();
    }
}
