FROM maven:3.6.3-jdk-11-slim

WORKDIR /app
COPY . .

RUN mvn clean install -DskipTests

RUN chmod +x /app/cert/*
RUN chmod +x /app/start.sh

WORKDIR /app/client
CMD ["bash", "-c", "/app/start.sh && mvn exec:java"]
