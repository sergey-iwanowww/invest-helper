FROM openjdk:17-jdk-slim as builder
COPY  './build/libs/invest-helper-0.0.1-SNAPSHOT.jar' /spring-boot/app.jar

RUN cd /spring-boot && java -Djarmode=layertools -jar app.jar extract

FROM openjdk:17-jdk-slim
EXPOSE 8080
RUN rm -f /etc/localtime && ln -sf /usr/share/zoneinfo/UTC  /etc/localtime && date
ENV LANG=ru_RU.UTF-8
ENV JAVA_OPTS: -Duser.country=RU -Duser.language=ru --enable-preview

CMD java $JAVA_OPTS org.springframework.boot.loader.JarLauncher

COPY  --from=builder /spring-boot/dependencies/ ./
RUN echo '.'
COPY  --from=builder /spring-boot/spring-boot-loader/ ./
RUN echo '..'
COPY  --from=builder /spring-boot/snapshot-dependencies/ ./
RUN echo '...'
COPY  --from=builder /spring-boot/application/ ./


