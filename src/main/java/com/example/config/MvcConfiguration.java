package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer {
    @Override
    // Configure CORS settings
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS settings to all endpoints 所有后端接口可以被跨域。
                .allowedOrigins("*") // Allow all origins 允许任意来源（所有域名）的请求。
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // Allow all headers
                .exposedHeaders("Content-Disposition"); // 允许前端访问响应中的 Content-Disposition 头
    }
}
