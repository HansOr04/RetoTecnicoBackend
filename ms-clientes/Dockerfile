FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S banco && adduser -S banco -G banco

WORKDIR /app

COPY target/*.jar app.jar

RUN chown banco:banco /app/app.jar

USER banco

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
