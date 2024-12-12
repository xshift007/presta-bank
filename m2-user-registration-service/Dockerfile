FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} m2-user-registration-service.jar
ENTRYPOINT ["java","-jar","/m2-user-registration-service.jar"]
