FROM eclipse-temurin:8-jre
# Set the working directory
WORKDIR /app

# Copy the built Play Framework application from the build stage
COPY my-play-application-0.1.0 /app/my-play-application-0.1.0
CMD ["/app/my-play-application-0.1.0/bin/my-play-application"]

#Expose port 9000
EXPOSE 9000
