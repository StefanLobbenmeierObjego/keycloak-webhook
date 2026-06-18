package com.vymalo.keycloak.webhook.integration

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.println
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.Transferable

class KeycloakWebhookIntegrationTest {
    @Test
    fun `print docker diagnostics`() {
        println("DOCKER_HOST=" + System.getenv("DOCKER_HOST"))
        println("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=" + System.getenv("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE"))
        println("user.home=" + System.getProperty("user.home"))
        println("docker socket exists=" + Path.of(System.getProperty("user.home"), ".docker", "run", "docker.sock").exists())

        val availability = runCatching { DockerClientFactory.instance().isDockerAvailable }
        println("docker available result=" + availability.getOrNull())
        println("docker available error=" + availability.exceptionOrNull())
    }

    @Test
    fun `webhook admin ui endpoints are reachable after authentication`() {
        keycloakContainer().use { keycloak ->
            keycloak.start()

            val client = HttpClient.newHttpClient()
            val baseUrl = "http://${keycloak.host}:${keycloak.getMappedPort(8080)}/auth"
            val token = adminToken(client, baseUrl)

            val pageResponse = get(client, "$baseUrl/realms/master/webhook-ui", token, "text/html")
            assertEquals(200, pageResponse.statusCode(), pageResponse.body())
            assertTrue(pageResponse.body().contains("Webhook UI Stub"), pageResponse.body())

            val configResponse = get(client, "$baseUrl/realms/master/webhook-ui/config", token, "application/json")
            assertEquals(200, configResponse.statusCode(), configResponse.body())
            assertTrue(configResponse.body().contains("\"general\""), configResponse.body())
        }
    }

    private fun keycloakContainer(): GenericContainer<*> {
        val jars = providerJars()

        return GenericContainer("quay.io/keycloak/keycloak:26.4.0")
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
            .withEnv("KC_HTTP_RELATIVE_PATH", "/auth")
            .withExposedPorts(8080)
            .withCopyToContainer(Transferable.of(""), "/opt/keycloak/data/import/.keep")
            .apply {
                jars.forEach { jar ->
                    withCopyFileToContainer(
                        org.testcontainers.utility.MountableFile.forHostPath(jar),
                        "/opt/keycloak/providers/${jar.fileName}"
                    )
                }
            }
            .withCommand("start-dev")
            .waitingFor(
                Wait.forHttp("/auth/realms/master/.well-known/openid-configuration")
                    .forStatusCode(200)
            )
    }

    private fun providerJars(): List<Path> = listOf(
        jarPath("keycloak-webhook-provider-core"),
        jarPath("keycloak-webhook-provider-http"),
        jarPath("keycloak-webhook-provider-amqp"),
        jarPath("keycloak-webhook-provider-syslog")
    )

    private fun jarPath(moduleName: String): Path {
        val jar = Path.of(
            System.getProperty("user.dir"),
            "..",
            moduleName,
            "build",
            "libs",
            "$moduleName-0.10.0-rc.1-all.jar"
        ).normalize()

        check(jar.exists()) { "Missing provider jar: $jar" }
        return jar
    }

    private fun adminToken(client: HttpClient, baseUrl: String): String {
        val body = formUrlEncoded(
            "client_id" to "admin-cli",
            "username" to "admin",
            "password" to "admin",
            "grant_type" to "password"
        )

        val response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/realms/master/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        assertEquals(200, response.statusCode(), response.body())
        return Regex("\"access_token\"\\s*:\\s*\"([^\"]+)\"")
            .find(response.body())
            ?.groupValues
            ?.get(1)
            ?: error("No access token in response: ${response.body()}")
    }

    private fun get(
        client: HttpClient,
        url: String,
        token: String,
        accept: String,
    ): HttpResponse<String> = client.send(
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer $token")
            .header("Accept", accept)
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString()
    )

    private fun formUrlEncoded(vararg entries: Pair<String, String>): String = entries.joinToString("&") {
        "${it.first.urlEncode()}=${it.second.urlEncode()}"
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)
}
