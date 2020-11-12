FROM openjdk:15-oracle
EXPOSE 3000 #change o
ARG JAR_FILE=target/payment-api.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
