FROM clojure:temurin-21-lein as builder

# Set the working directory inside the container
WORKDIR /app

COPY project.clj /app/

RUN lein deps

# Copy project files into the working directory
COPY . /app

# Build the JAR file using Leiningen
RUN lein uberjar

# Step 2: Create the runtime container
FROM eclipse-temurin:21

# Set the working directory in the new container
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/uberjar/*-standalone.jar /app/app.jar
# COPY --from=builder /app/entrypoint.sh /app/entrypoint.sh
# RUN chmod +x /app/entrypoint.sh

# Run the JAR file, potentially using the environment variables
ENTRYPOINT ["java", "-jar", "-Xmx4000m", "/app/app.jar"]