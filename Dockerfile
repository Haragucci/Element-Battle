# Verwende das offizielle OpenJDK 21-Image als Basis
FROM openjdk:21-jdk-slim

# Setze das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere die Gradle Wrapper Dateien und den Gradle build script
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Stelle sicher, dass gradlew ausführbar ist
RUN chmod +x gradlew

# Kopiere den Quellcode in das Arbeitsverzeichnis
COPY src/ src/

# Führe den Gradle build Befehl aus
RUN ./gradlew build

# Kopiere das erstellte JAR-File und alle JSON-Dateien in das Arbeitsverzeichnis des Containers
COPY build/libs/*.jar /app/
COPY build/libs/*.json /app/

# Definiere den Befehl zum Starten der Anwendung
CMD ["java", "-jar", "/app/demo1-0.0.1-SNAPSHOT.jar"]
