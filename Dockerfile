FROM adoptopenjdk/openjdk11:alpine-jre
ARG JAR_FILE=target/keycloak-services-0.0.1-SNAPSHOT.jar
WORKDIR /opt/app
COPY ${JAR_FILE} keycloak-services.jar
ENTRYPOINT ["java","-jar","keycloak-services.jar"]