FROM amazoncorretto:11-alpine
ARG version
ARG JAR_FILE=build/libs/bpartners-api-$version.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar", "-Djdk.httpclient.HttpClient.log=all"]
EXPOSE 8080
