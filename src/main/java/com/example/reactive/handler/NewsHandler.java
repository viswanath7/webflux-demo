package com.example.reactive.handler;

import com.example.reactive.domain.News;
import com.example.reactive.repository.NewsRepository;
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
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NewsHandler {

    private final NewsRepository newsRepository;

    public NewsHandler(final NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public Mono<ServerResponse> listNews(final ServerRequest serverRequest) {
        log.debug("Handling request: {}", serverRequest);
        Flux<News> newsFlux = newsRepository.findAll().retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
        return ServerResponse
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).noTransform().cachePublic())
                .contentType(MediaType.APPLICATION_JSON)
                .body(newsFlux, News.class)
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
