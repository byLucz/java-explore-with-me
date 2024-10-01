FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar stat-server-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/stat-server-0.0.1-SNAPSHOT.jar"]