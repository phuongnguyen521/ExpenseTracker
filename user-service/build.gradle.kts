plugins {
    id("java")
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.phuong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Dependency
    implementation(project(mapOf("path" to ":expense-service")))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.5")

    // Database
    implementation("org.postgresql:postgresql")
    // implementation("com.mysql:mysql-connector-j")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.3")

    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.3.0")

    // Logging and Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.5")
    implementation("io.micrometer:micrometer-registry-prometheus:1.15.0")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation:3.4.5")

    // junit
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}