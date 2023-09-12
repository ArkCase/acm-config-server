FROM maven:3.8-adoptopenjdk-11 as build

WORKDIR /build
COPY . .
RUN mvn clean install

FROM openjdk:11.0.16

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
