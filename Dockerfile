FROM openjdk:8 AS JVAMP_BUILD
LABEL maintainer="Osipiuk Bartosz <osipiuk.bartosz@gmail.com>"
ENV APP_JVAMP_BUILD_HOME=/usr/app/
WORKDIR $APP_JVAMP_BUILD_HOME
COPY soundscribe-be/docker/build-docker.sh tmp/build-docker.sh
RUN apt-get update && \
    apt-get -y install cmake
RUN cd tmp && \
    ./build-docker.sh
RUN git clone https://github.com/rism-ch/verovio.git tmp/verovio \
    && (cd tmp/verovio/tools && cmake . && make && make install)

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
COPY --from=JVAMP_BUILD /usr/app/tmp/jvamp-1.3/libvamp-jni.so /usr/lib/libvamp-hostsdk.so.3 /usr/lib/
#   important line
RUN cp /usr/lib/libvamp-hostsdk.so.3 /usr/lib/libvamp-hostsdk.so
COPY --from=JVAMP_BUILD /usr/local/lib/vamp/ /usr/lib/vamp/
COPY --from=JVAMP_BUILD /usr/local/bin/verovio /usr/local/bin/verovio
COPY --from=APP_BUILD /usr/app/soundscribe-be/build/libs/soundscribe-1.0.jar .
COPY soundscribe-be/libs/python/ /usr/tmp/
RUN mv /usr/tmp/abc2xml.py /usr/local/bin/abc2xml && \
    mv /usr/tmp/xml2abc.py /usr/local/bin/xml2abc && \
    curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py && \
    python get-pip.py && \
    python -m pip install pyparsing
RUN apt update && apt install openjdk-8-jdk -y
CMD ["java", "-jar", "/usr/app/soundscribe-1.0.jar"]
