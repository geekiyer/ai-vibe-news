FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY /app/build/libs/ai-vibe-news-all.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"] 