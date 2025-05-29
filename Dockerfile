FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the application jar
COPY build/libs/*.jar app.jar

# Copy database if it exists (or it will be created at runtime)
COPY data.db* /app/

# Copy static files
COPY public/static /app/public/static

# Expose the port your application runs on
EXPOSE 8080

# Set environment variables (can be overridden at runtime)
ENV ADMIN_USERNAME=admin
ENV ADMIN_PASSWORD=pass

# Run the application
CMD ["java", "-jar", "app.jar"]
