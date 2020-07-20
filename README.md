# Introduction

The reactive web-application built using Spring's **Webflux** and **R2DBC** on Java 11 exposes the following endpoints using HTTP GET method. 

 - /health-check
 - /news/{newsType}?numberOfItems={positiveNumber}

Supported values for optional **newsType** path parameter are 
 * top
 * best
 * latest
The default value of **newsType** path parameter is **top**


The default value of optional query parameter **numberOfItems** is 3. Supplied value must be in the range 1 to 1024. 


# Usage guide


 ## Create a docker image and launch it
 
1. Create a docker image as shown below 

```bash
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=bloque/reactive-hacker-news:1
```

2. Launch the created image as shown below 
 
 ```bash
docker run -e "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n" -p 8080:8080 -p 5005:5005 -it bloque/reactive-hacker-news:1
```

3. Go to URL <http://localhost:8080/news/best?numberOfItems=3>
