### Local linux/docker usage
# docker build --rm -t xchess .
# docker run --rm -it --name xchess -p 8080:8080 xchess

### BUILD
FROM hseeberger/scala-sbt:eclipse-temurin-17.0.2_1.6.2_2.13.8 AS build

# /root is the standard dir of the hseeberger/scala-sbt images
WORKDIR /root

# Configure SBT
ENV SBT_OPTS "-Xmx256M -Xss2M -Dfile.encoding=utf8 -Dsbt.log.noformat=true"

# Prepare SBT and download dependencies
COPY server/build.sbt .
COPY server/project/build.properties project/
RUN sbt update

# Build - collects all JARs in /root/target/universal/jars
COPY server/src src
RUN sbt collectJars

### PRODUCTION IMAGE
FROM openjdk:17.0.2-slim AS prod

# Server install location, workdir for entrypoint
WORKDIR /opt/xchess/server

# Copy server
COPY --from=build /root/target/universal/jars lib

# Copy client
COPY client ../client

# We don't want to run the service as root user
RUN useradd -lMs /bin/bash docker
USER docker
CMD [ "java", "-cp", "lib/*", "-Xmx64M", "-XX:+ExitOnOutOfMemoryError", "xchess.Main" ]
