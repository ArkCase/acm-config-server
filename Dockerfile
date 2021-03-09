FROM maven:3.6-jdk-8 as build

WORKDIR /build
COPY . .
RUN mvn clean install


FROM openjdk:8-jdk

WORKDIR /app
RUN useradd --create-home --user-group arkcase \
        && mkdir /app/tmp \
        && chown -R arkcase:arkcase /app

USER arkcase
COPY --from=build /build/target/config-server-*-SNAPSHOT.jar \
        /app/config-server.jar
COPY docker/run.sh /app/run.sh

EXPOSE 9999
CMD [ "/app/run.sh" ]
