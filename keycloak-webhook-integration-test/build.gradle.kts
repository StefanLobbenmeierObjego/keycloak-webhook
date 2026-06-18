plugins {
    kotlin("jvm")
}

group = "com.vymalo.keycloak.webhook"
version = "0.10.0-rc.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.1"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()

    dependsOn(
        ":keycloak-webhook-provider-core:shadowJar",
        ":keycloak-webhook-provider-http:shadowJar",
        ":keycloak-webhook-provider-amqp:shadowJar",
        ":keycloak-webhook-provider-syslog:shadowJar"
    )
}
