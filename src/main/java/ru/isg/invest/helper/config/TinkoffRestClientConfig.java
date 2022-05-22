package ru.isg.invest.helper.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by s.ivanov on 20.11.2021.
 */
@Configuration
@Slf4j
public class TinkoffRestClientConfig {

    @Value("${tinkoff-api.token}")
    private String token;

    /**
     * Возвращает Interceptor для проведения аутентификации.
     */
    @Bean
    public RequestInterceptor authInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
