package com.example.reactive.configuration;

import com.example.reactive.handler.NewsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
@EnableWebFlux
@Slf4j
public class WebConfiguration implements WebFluxConfigurer {

    @Bean
    public WebClient webClient(@Value( "${hackernews.service.base.url:https://hacker-news.firebaseio.com/v0}" ) final String serviceBaseURL) {
        return WebClient.create(serviceBaseURL.trim());
    }

    @Bean
    public RouterFunction<ServerResponse> route(final NewsHandler newsHandler) {
        return RouterFunctions.route()
                .POST("/news", accept(APPLICATION_JSON), newsHandler::listNews)
                .after((request, response) -> {
                    log.debug("HTTP response: {}", response);
                    return response;
                })
                .build();
    }

}
