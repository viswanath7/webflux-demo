package com.example.reactive.repository;

import com.example.reactive.domain.News;
import com.example.reactive.domain.NewsType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface NewsRepository extends ReactiveCrudRepository<News, Long> {
    Mono<Long> countByNewsTypeContainsAndDateEquals(NewsType newsType, LocalDate date);
    Flux<News> findByNewsTypeContainsAndDateEquals(NewsType newsType, LocalDate date);
}
