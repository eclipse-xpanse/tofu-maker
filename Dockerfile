FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S tofu-maker && adduser -S -G tofu-maker tofu-maker
RUN apk update && \
    apk add --no-cache unzip wget
ENV OPENTOFU_VERSION=1.6.0
RUN wget https://github.com/opentofu/opentofu/releases/download/v${OPENTOFU_VERSION}/tofu_${OPENTOFU_VERSION}_linux_amd64.zip
RUN unzip tofu_${OPENTOFU_VERSION}_linux_amd64.zip
RUN mv tofu /usr/bin/tofu
COPY target/tofu-maker-*.jar tofu-maker.jar
USER tofu-maker
ENTRYPOINT ["java","-jar","tofu-maker.jar"]