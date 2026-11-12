package com.example.shopping_project.config.productconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;

/**
 * 쿼리 파라미터 minPrice=/maxPrice= 빈 문자열을 null 로 변환한다.
 * (HomeController 수정 없이 금액 필터 바인딩 안정화)
 */
@Configuration
public class BigDecimalConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, BigDecimal>() {
            @Override
            public BigDecimal convert(String source) {
                if (source == null || source.isBlank()) {
                    return null;
                }
                return new BigDecimal(source.trim());
            }
        });
    }
}
