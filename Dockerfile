FROM openjdk:21-slim

WORKDIR /app

COPY build/libs/ai-vibe-news-all.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"] 