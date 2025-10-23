# Use a multi-stage build to create a lean final image

# --- Build Stage ---
# Use a standard Maven image to build the application .jar file
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, creating the executable .jar
# The Vaadin frontend will be built in production mode via the pom.xml plugin
RUN mvn clean install -DskipTests

# --- Run Stage ---
# Use a slim Java runtime image for a smaller and more secure final image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the executable .jar file from the build stage
# The name 'alexia-1.0.0.jar' comes from pom.xml <artifactId> and <version>
COPY --from=build /app/target/alexia-1.0.0.jar ./app.jar

# Expose the port the application will run on. 
# Render provides the PORT env var, which the app is configured to use.
EXPOSE 8080

# Set the command to run the application
# The DATABASE_* and other credentials will be passed as environment variables by Render
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
