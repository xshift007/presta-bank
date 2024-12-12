FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} m1-credit-simulation-service.jar
ENTRYPOINT ["java","-jar","/m1-credit-simulation-service.jar"]
