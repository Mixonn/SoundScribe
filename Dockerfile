FROM openjdk:8 AS JVAMP_BUILD
LABEL maintainer="Osipiuk Bartosz <osipiuk.bartosz@gmail.com>"
ENV APP_JVAMP_BUILD_HOME=/usr/app/
WORKDIR $APP_JVAMP_BUILD_HOME
# Should be replaced to git clone when repo become public
COPY soundscribe-be/docker/build-docker.sh tmp/build-docker.sh
RUN cd tmp && ./build-docker.sh

FROM openjdk:11 AS APP_BUILD
LABEL maintainer="Osipiuk Bartosz <osipiuk.bartosz@gmail.com>"
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY . .
RUN ./gradlew build


FROM openjdk:11 AS APP_RUN
LABEL maintainer="Osipiuk Bartosz <osipiuk.bartosz@gmail.com>"
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=JVAMP_BUILD /usr/app/tmp/jvamp-1.3/libvamp-jni.so /usr/lib/libvamp-hostsdk.so /usr/lib/
COPY --from=APP_BUILD /usr/app/soundscribe-be/build/libs/soundscribe-1.0.jar .
CMD ["java", "-jar", "/usr/app/soundscribe-1.0.jar"]
