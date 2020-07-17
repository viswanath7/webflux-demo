package com.example.reactive;

import com.example.reactive.domain.NewsType;
import com.example.reactive.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Slf4j
public class ReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			final ConfigurableApplicationContext applicationContext,
			final NewsService newsService) {
		return args -> {
			log.info("Let's fetch 3 latest hacker news stories ...");


			for (int counter= 0; counter < 2; counter++) {
				newsService.getNews(NewsType.LATEST, 3)
						/*.doOnComplete( () -> {
							log.info("Terminating the application ...");
							SpringApplication.exit(applicationContext, () -> 0);
						} )*/
						.toStream()
						.forEach( hackerNewsStory -> log.info("Hacker news story identifier: {}", hackerNewsStory) );
			}


		};
	}

}
