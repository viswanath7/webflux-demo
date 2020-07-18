package com.example.reactive.service;

import com.example.reactive.domain.News;
import com.example.reactive.domain.NewsType;
import com.example.reactive.repository.NewsRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private final HackerNewsService hackerNewsService;

    public NewsService(final NewsRepository newsRepository,
                       final HackerNewsService hackerNewsService) {
        this.newsRepository = newsRepository;
        this.hackerNewsService = hackerNewsService;
    }

    public Flux<News> getNews(@NonNull final NewsType newsType,
                              @Min(1) @Max(1024) final Integer numberOfItems) {
        log.debug("Fetching {} news of type {} ...", numberOfItems, newsType);
        final var today = LocalDate.now();
        final var resultFromDatabase = newsRepository
                .findByNewsTypeContainsAndDateEquals(newsType, today)
                .distinct(News::getIdentifier);
        return newsRepository.countByNewsTypeContainsAndDateEquals(newsType, today)
                .filter(count -> {
                    final var result = numberOfItems <= count;
                    log.debug("Does database already has sufficient records? {}", result ? "Yes" : "No");
                    return result;
                })
                .flatMapMany(c -> resultFromDatabase.take(numberOfItems))
                //.doOnEach(newsSignal -> log.debug("News fetched from database: {}", newsSignal.get()))
                .switchIfEmpty(getNewsFromWeb(newsType, numberOfItems));
    }

    private Flux<News> getNewsFromWeb(@NonNull NewsType newsType, @Min(1) @Max(1024) Integer numberOfItems) {
        final Flux<News> resultFromWeb = hackerNewsService.getNewsIdentifiers(newsType, numberOfItems)
                .flatMap(hackerNewsService::getNews)
                .map(hackerNewsStory -> new News(newsType, hackerNewsStory))
                .onBackpressureBuffer(256)
                .onErrorStop()
                .sort(Comparator.comparing(News::getDate));
        var brandNew = resultFromWeb
                .filterWhen(news -> newsRepository.existsById(Objects.requireNonNull(news.getId())).map(res -> !res));
        var preExisting = resultFromWeb
                .filterWhen(news -> newsRepository.existsById(Objects.requireNonNull(news.getId())))
                .map(news -> {
                    final Set<NewsType> updatedNewsType = new HashSet<>(news.getNewsType());
                    updatedNewsType.add(newsType);
                    news.setNewsType(updatedNewsType);
                    news.setNewEntity(false);
                    return news;
                });
        return newsRepository.saveAll(brandNew.concatWith(preExisting)).take(numberOfItems);
    }
}
