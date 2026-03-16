# ---------- Build Stage ----------
# 就是要做 RUN mvn clean package 的前置作業

FROM maven:3.9-eclipse-temurin-17 AS builder

# 在容器內建立並進入 /build 資料夾，後續指令都在這裡執行。
WORKDIR /build

# 把專案根目錄的 pom.xml 複製到容器的 /build 資料夾。
COPY pom.xml .


RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY src ./src

# build war 跳過 單元測試 -DskipTests
RUN mvn clean package -DskipTests


# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]