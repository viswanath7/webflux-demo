package com.example.reactive.service;

import com.example.reactive.domain.News;
import com.example.reactive.domain.NewsType;
import com.example.reactive.repository.NewsRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.Duration;
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
        Flux<News> resultFromDatabase =
                newsRepository.findByNewsTypeContainsAndDateEquals(newsType, today)
                        .distinct(News::getIdentifier)
                        .doOnEach(newsSignal -> log.debug("News fetched from database: {}", newsSignal.get()));
        return resultFromDatabase.transform(newsFlux -> {
            if (newsFlux.count().map(count -> numberOfItems <= count).blockOptional(Duration.ofSeconds(2)).orElse(false)) {
                log.debug("The database has sufficient news items of type '{}' for date {} so, utilising it instead of making a web-service call.", newsType, today);
                return resultFromDatabase.take(numberOfItems);
            } else {
                log.debug("The database did NOT have {} news items of type {} for date {}", numberOfItems, newsType, today);
                // Fetching the news for supplied type expecting for today. The expectation is that hacker news would have today's date in time property
                final Flux<News> resultFromWeb = hackerNewsService.getNewsIdentifiers(newsType, numberOfItems)
                        .flatMap(hackerNewsService::getNews)
                        .map(hackerNewsStory -> new News(newsType, hackerNewsStory))
                        .onBackpressureBuffer(256)
                        .cache(Duration.ofMinutes(5))
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
                return newsRepository.saveAll(brandNew.concatWith(preExisting));
            }
        });
    }
}
