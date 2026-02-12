package com.example.naebuilding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();

        // ✅ 핵심: file: 로 시작 + 디렉토리 끝에 "/" 붙이기
        String location = "file:" + dir.toString() + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
