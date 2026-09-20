# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy just the POM first, so Docker caches the downloaded dependencies in
# their own layer. This layer only gets invalidated when pom.xml itself
# changes -- not on every source code edit, which is most edits.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the actual source and build. This layer WILL be invalidated on
# every code change, but by then dependencies are already cached above it.
COPY src ./src
RUN mvn package -DskipTests -B

# ---- Stage 2: run the jar ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Only the finished artifact crosses into this image -- none of Maven, the
# JDK compiler, or the dependency cache from Stage 1 comes with it. The
# wildcard avoids hardcoding the exact version string here.
COPY --from=build /build/target/jredis-*.jar app.jar

EXPOSE 6380

# -XX:MaxRAMPercentage, not a hardcoded -Xmx: modern JDKs (17 here) are
# container-aware and read the container's own memory limit (its cgroup),
# but still need to be told what percentage of that visible limit to use
# for the heap. This way the same image behaves sensibly whether the
# container is given 512MB or 4GB, with no rebuild needed either way.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]