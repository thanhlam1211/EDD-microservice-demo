FROM openjdk:17
RUN mkdir /app
COPY target/**.jar app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","/app.jar"]