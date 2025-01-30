FROM openjdk:17-slim
USER root
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
COPY opennlp /app/opennlp
EXPOSE 8080
ENTRYPOINT ["java","-jar","application.jar"]