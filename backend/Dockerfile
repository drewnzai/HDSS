FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        gnupg; \
    install -d -m 0755 /usr/share/keyrings; \
    curl -fsSL \
        "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0xB7B3B788A8D3785C" \
        -o /tmp/mysql-key.asc; \
    gpg --show-keys --with-colons /tmp/mysql-key.asc \
        | awk -F: '$1 == "fpr" { print $10; exit }' \
        | grep -qx "BCA43417C3B485DD128EC6D4B7B3B788A8D3785C"; \
    gpg --dearmor \
        -o /usr/share/keyrings/mysql-archive-keyring.gpg \
        /tmp/mysql-key.asc; \
    chmod 644 /usr/share/keyrings/mysql-archive-keyring.gpg; \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/mysql-archive-keyring.gpg] https://repo.mysql.com/apt/ubuntu/ jammy mysql-8.4-lts" \
        > /etc/apt/sources.list.d/mysql.list; \
    apt-get update; \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        mysql-community-client; \
    rm -rf /var/lib/apt/lists/* /tmp/mysql-key.asc

COPY --from=build /app/target/hdss-*.jar app.jar
COPY downloads ./downloads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
