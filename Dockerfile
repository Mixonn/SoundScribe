ARG SOUNDSCRIBE_VERSION=1.0
ARG JAVA_VERSION=8

FROM openjdk:${JAVA_VERSION} AS BUILD
ENV APP_BUILD_HOME=/usr/app/
WORKDIR $APP_BUILD_HOME
# Should be replaced to git clone when repo become public
COPY soundscribe-be/docker/build-docker.sh tmp/build-docker.sh
COPY . .
RUN cd tmp && ./build-docker.sh
RUN ./gradlew build

#
FROM openjdk:${JAVA_VERSION} AS RUN_APP
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=BUILD /$APP_BUILD_HOME/soundscribe-be/build/libs/soundscribe-$SOUNDSCRIBE_VERSION.jar .
COPY --from=BUILD /$APP_BUILD_HOME/tmp ./tmp
COPY soundscribe-be/docker/build-docker.sh tmp/build-docker.sh
RUN cd tmp && ./build-docker.sh
CMD ["java", "-jar", "soundscribe-be/build/libs/soundscribe-$SOUNDSCRIBE_VERSION.jar"]
EXPOSE 8080
