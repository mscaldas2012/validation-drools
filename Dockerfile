# build stage
FROM maven:3-jdk-11 as builder
RUN mkdir -p /usr/src/app
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn clean package -DskipTests=true

# create Image stage
FROM adoptopenjdk:jdk-11.0.7_10-alpine

VOLUME /tmp

COPY --from=builder  /usr/src/app/target/validation-drools-*.jar ./validation-drools.jar

RUN sh -c 'touch ./validation-drools.jar'

ENTRYPOINT ["java","-server","-Xms256m","-Xmx2g","-XX:MaxMetaspaceSize=256m","-Djava.security.egd=file:/dev/./urandom","-jar","./validation-drools.jar"]