FROM java:8-jdk-alpine
MAINTAINER heloise
COPY build/libs/tourGuide-1.0.0.jar tourGuide-1.0.0.jar
ENTRYPOINT ["java", "-jar", "/tourGuide-1.0.0.jar"]
EXPOSE 8080