# Stage 1: Get Java 21 Runtime from Eclipse Temurin
FROM eclipse-temurin:21-jre AS java21

# Stage 2: Final Node-based image
FROM node:20-slim

# Install dependencies (only curl is needed)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Java 21 from the first stage
COPY --from=java21 /opt/java/openjdk /opt/java/openjdk

# Set Environment Variables for Java
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Install firebase-tools globally
RUN npm install -g firebase-tools

# Set the working directory
WORKDIR /app

# Set non-interactive mode for CI
ENV CI=true

# Expose ports based on firebase.json
EXPOSE 8080 8085 4000

# Entrypoint to run the emulators
CMD ["firebase", "emulators:start", "--only", "firestore,pubsub", "--project", "lifelog-dev", "--import", "/app/data", "--export-on-exit", "/app/data"]
