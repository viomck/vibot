FROM amazoncorretto:17

COPY build/libs/vibot-0.0.1-SNAPSHOT.jar vibot.jar
CMD ["java", "-jar", "vibot.jar"]
