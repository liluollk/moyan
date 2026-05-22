package com.liluo.moyan.framework.config;

import com.liluo.moyan.framework.security.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置 - 注册拦截器 + 静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Value("${file.storage.local-path:uploads}")
    private String localPath;

    public WebMvcConfig(JwtAuthenticationInterceptor jwtAuthenticationInterceptor) {
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/send-code",
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/public/**",
                        "/api/search/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到本地磁盘目录，支持本地文件存储模式
        String userDir = System.getProperty("user.dir");
        String absolutePath = Paths.get(userDir, localPath).toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
