FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/* /target/

CMD ["java","-Dspring.profiles.active=container","-jar","/target/connectionstatistics-0.0.1-SNAPSHOT.jar"]