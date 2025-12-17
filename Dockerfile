FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

#Copiar solo el pom para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

#Copiar el código y compilar
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
