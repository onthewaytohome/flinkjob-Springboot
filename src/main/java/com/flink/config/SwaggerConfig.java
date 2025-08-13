package com.flink.config;
 
import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @ClassName: Swagger2
 * @description: 系统API接口文档配置类
 * @create: 2021-08-25 14:06:10
 * @Version 1.1.0.1
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    //是否开启swagger，正式环境一般是需要关闭的，可根据springboot的多环境配置进行设置
    //@Value(value = "${custom.swagger.enabled}")
    Boolean swaggerEnabled = true;
 
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                // 是否开启
                .enable(swaggerEnabled).select()
                // 扫描的路径包
                .apis(Predicates.or(RequestHandlerSelectors.withClassAnnotation(RestController.class)))
                //排除某些不需要映射的路径
                .paths(Predicates.not(PathSelectors.regex("/push.*")))
                // 指定路径处理PathSelectors.any()代表所有的路径
                .paths(PathSelectors.any())
                .build().pathMapping("/");
    }
 
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("flink-cdc服务API")
                .description("接口文档说明")
                .version("v1")
                .build();
    }
 
}