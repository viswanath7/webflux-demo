package com.example.reactive.handler;

import com.example.reactive.domain.News;
import com.example.reactive.domain.NewsType;
import com.example.reactive.repository.NewsRepository;
import com.example.reactive.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NewsHandler {

    private final NewsService newsService;

    public NewsHandler(final NewsService newsService) {
        this.newsService = newsService;
    }

    public Mono<ServerResponse> listNews(final ServerRequest serverRequest) {
        log.debug("Handling request: {}", serverRequest);
        final var newsType = NewsType.valueOf(Optional.of(serverRequest.pathVariable("newsType")).orElse("top").trim().toUpperCase());
        final var numberOfItems = Integer.parseInt(serverRequest.queryParam("numberOfItems").orElse("10"));
        final var newsFlux = newsService.getNews(newsType, numberOfItems)
                .cache(Duration.ofMinutes(5))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)));
        return ServerResponse
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).noTransform().cachePublic())
                .contentType(MediaType.APPLICATION_JSON)
                .body(newsFlux, News.class)
                .switchIfEmpty(ServerResponse.noContent().build());
    }
}
