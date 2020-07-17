package com.example.reactive.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table("NEWS")
public class News implements Persistable<Long> {
    @Id
    @Column("id")
    private Long identifier;
    private String title;
    @Column("story_date")
    private LocalDate date;
    @Column("news_type")
    private Set<NewsType> newsType;
    private String content;
    private String hyperlink;
    private Integer score;
    @ToString.Exclude
    private List<Long> children;
    // See https://docs.spring.io/spring-data/jdbc/docs/current/reference/html/#jdbc.entity-persistence.state-detection-strategies
    @Transient
    private boolean newEntity;

    public News(@NonNull final NewsType newsType, @NonNull final HackerNewsStory hackerNewsStory, final boolean isNewEntity) {
        this(hackerNewsStory.getId(), hackerNewsStory.getTitle(),
                LocalDateTime.ofEpochSecond(hackerNewsStory.getTime(), 0, ZoneOffset.UTC).toLocalDate(),
                Set.of(newsType),
                hackerNewsStory.getText(), hackerNewsStory.getUrl(),
                hackerNewsStory.getScore(), hackerNewsStory.getKids(), isNewEntity );
    }

    public News(@NonNull final NewsType newsType, @NonNull final HackerNewsStory hackerNewsStory) {
        this(newsType, hackerNewsStory, true);
    }

    @Override
    public Long getId() {
        return getIdentifier();
    }

    @Override
    public boolean isNew() {
        return isNewEntity();
    }
}
