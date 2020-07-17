package com.example.reactive.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HackerNewsStory {
    @Positive
    private Long id;
    @NotBlank
    private String by;
    @PositiveOrZero
    private Integer descendants;
    private List<Long> kids;
    @PositiveOrZero
    private Integer score;
    @Positive
    private Long time;
    @NotBlank
    private String title;
    @NotBlank
    private String type;
    private String url;
    private String text;

    @Override
    public String toString() {
        final var urlPrintable = StringUtils.isNotBlank(url) ? ", url='" + url.trim() + '\'': "";
        final var textPrintable = StringUtils.isNotBlank(text) ? ", text='" + text.trim() + '\'': "";
        return "HackerNewsStory{ id=" + id +
                ", by='" + by + '\'' +
                ", score=" + score +
                ", time=" + time +
                ", title='" + title + '\''
                + urlPrintable + textPrintable +
                '}';
    }
}
