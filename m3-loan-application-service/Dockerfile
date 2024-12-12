FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} m3-loan-application-service.jar
ENTRYPOINT ["java","-jar","/m3-loan-application-service.jar"]
