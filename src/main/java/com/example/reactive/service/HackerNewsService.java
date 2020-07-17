package com.example.reactive.service;

import com.example.reactive.domain.HackerNewsStory;
import com.example.reactive.domain.NewsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@Slf4j
/*
 * A client for Hacker news API.
 * See https://github.com/HackerNews/API for details
 */
public class HackerNewsService {

    private final WebClient webClient;

    public HackerNewsService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Retrieves News of supplied type from Hacker news
     *
     * @param newsType  Type of news to fetch
     * @return  Publisher of HackerNewsStory
     */
    public Flux<Integer> getNewsIdentifiers(@NonNull final NewsType newsType,
                                            @Min(1) @Max(1024) final Integer numberOfItems) {
        log.debug("Fetching {} news stories of type {} from the web ...", numberOfItems, newsType);
        return webClient.get()
                .uri(newsType.getIdentifiersPath())
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
                .timeout(Duration.ofSeconds(5))
                .flatMapMany( response -> response.bodyToFlux(Integer.class))
                .distinct()
                .take(numberOfItems);
    }

    public Mono<HackerNewsStory> getNews(@NonNull final Integer identifier) {
        log.debug("Fetching story with identifier {} from the web ...", identifier);
        return webClient.get()
                .uri(String.format("/item/%d.json?print=pretty", identifier))
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
                .timeout(Duration.ofSeconds(5))
                .cache(Duration.ofMinutes(5))
                .flatMap( clientResponse -> clientResponse.bodyToMono(HackerNewsStory.class));
    }
}
