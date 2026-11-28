# 백엔드(Spring Boot) 컨테이너 이미지
# 사용처: 무료 테스트 환경(Render) · 이후 AWS(EC2 + Docker). 실행 방법은 docs/ops/deploy.md 참고

# 1단계: 빌드 — Gradle로 실행 가능한 jar를 만든다 (테스트는 CI에서 이미 돌리므로 생략)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# 2단계: 실행 — JDK 없이 JRE만 있는 가벼운 이미지에서 jar만 실행한다
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring
COPY --from=build /app/build/libs/*.jar app.jar
USER spring
EXPOSE 8080
# 컨테이너 메모리 한도의 70%까지만 힙으로 쓴다 (Render 무료 512MB 대비)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
ENTRYPOINT ["java", "-jar", "app.jar"]
