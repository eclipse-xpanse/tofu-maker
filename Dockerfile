FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S tofu-maker && adduser -S -G tofu-maker tofu-maker
RUN apk update && \
    apk add --no-cache unzip wget

ENV OPENTOFU_INSTALL_PATH=/opt/opentofu
ENV DEFAULT_OPENTOFU_VERSION=1.6.0
ENV OPENTOFU_VERSIONS=1.6.0,1.7.0,1.8.0
COPY install_opentofu.sh /install_opentofu.sh
RUN chmod +x /install_opentofu.sh
RUN echo "Downloading and installing OpenTofu with multiple versions $OPENTOFU_VERSIONS into path $OPENTOFU_INSTALL_PATH"; \
    /install_opentofu.sh "$OPENTOFU_INSTALL_PATH" "$DEFAULT_OPENTOFU_VERSION" "$OPENTOFU_VERSIONS"

COPY target/tofu-maker-*.jar tofu-maker.jar
USER tofu-maker
ENTRYPOINT ["java", "-Dopentofu.install.dir=${OPENTOFU_INSTALL_PATH}", "-Dopentofu.default.supported.versions=${OPENTOFU_VERSIONS}", "-jar", "tofu-maker.jar"]