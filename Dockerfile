# ---------- Tahap 1: build (Maven + JDK 25) ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Vaadin 25: "package" otomatis membuat bundle frontend produksi
RUN mvn -B -DskipTests package

# ---------- Tahap 2: runtime (JRE saja, lebih kecil) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Batasi heap supaya muat di RAM 512 MB (paket Free)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65"
# Render mengisi variabel PORT; application.yaml sudah membacanya
CMD ["java", "-jar", "app.jar"]