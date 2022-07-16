package com.yunpan.config;

import com.yunpan.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] path = {
                "/**",
        };
        String[] exclude = {
                "/js/**",
                "/img/**",
                "/css/**",
                "/plugins/**",
        };
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns(path).excludePathPatterns(exclude);
    }
}

