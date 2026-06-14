FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copier le pom d'abord pour profiter du cache Docker des dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le source et builder
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== STAGE 2 : RUN =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Créer un user non-root pour la sécurité
RUN addgroup -S delivery && adduser -S delivery -G delivery

# Créer le dossier de logs
RUN mkdir -p /app/logs && chown -R delivery:delivery /app

# Copier le jar buildé
COPY --from=builder /app/target/*.jar app.jar

# Passer en user non-root
USER delivery

# Exposer le port
EXPOSE 8080

# Variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Healthcheck Docker
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/actuator/health || exit 1

# Lancement
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]