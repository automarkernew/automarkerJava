package com.tagging.config;

import com.tagging.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${minio.localUrl}")
    private String minioLocalUrl;

    private String filePath = minioLocalUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //将所有 accessPath 访问都映射到 localFolder 路径下
        WebMvcConfigurer.super.addResourceHandlers(registry);
        registry.addResourceHandler("/view/**").addResourceLocations("file:" + minioLocalUrl + "/");

    }
    //用于添加拦截规则
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(getTokenInterceptor())
                .addPathPatterns("/**").excludePathPatterns("/login/login","/login/register", "/view/**");
    }


    @Bean
    public TokenInterceptor getTokenInterceptor(){
        return new TokenInterceptor();
    }
}
