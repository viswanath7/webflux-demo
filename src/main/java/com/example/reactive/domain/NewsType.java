package com.example.reactive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum NewsType {
    TOP("/topstories.json?print=pretty"),
    BEST("/beststories.json?print=pretty"),
    LATEST("/newstories.json?print=pretty");

    final String identifiersPath;

    NewsType(final String identifiersPath) {
        this.identifiersPath = identifiersPath;
    }

    public String getIdentifiersPath() {
        return identifiersPath;
    }
}
