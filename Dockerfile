# Use an appropriate base image with Java and other dependencies
FROM adoptopenjdk:11-jre-hotspot

# Install supervisor
RUN apt-get update && apt-get install -y supervisor

# Set the working directory in the container
WORKDIR /app

# Copy the microservices JAR files into the container
COPY dispatcher/target/dispatcher-1.0-SNAPSHOT.jar dispatcher.jar
COPY node/target/node-1.0-SNAPSHOT.jar node.jar
COPY rest-service/target/rest-service-1.0-SNAPSHOT.jar rest-service.jar
COPY mail-service/target/mail-service-1.0-SNAPSHOT.jar mail-service.jar

# Copy the modules JAR files into the container
COPY common-jpa/target/common-jpa-1.0-SNAPSHOT.jar common-jpa.jar
COPY common-rabbitmq/target/common-rabbitmq-1.0-SNAPSHOT.jar common-rabbitmq.jar
COPY common-utils/target/common-utils-1.0-SNAPSHOT.jar common-utils.jar

# Expose the ports on which the microservices run
EXPOSE 8084 8085 8086 8087

# Configure supervisord
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Run supervisord when the container starts
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]