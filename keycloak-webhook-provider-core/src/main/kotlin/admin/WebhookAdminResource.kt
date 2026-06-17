package com.vymalo.keycloak.webhook.core.admin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator

@Provider
class WebhookAdminResource(
    private val session: KeycloakSession,
    private val realm: RealmModel,
    private val auth: AdminPermissionEvaluator,
) {
    companion object {
        private const val CONFIG_ATTRIBUTE = "webhook.ui.config"
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        private val pageTemplate: String by lazy {
            WebhookAdminResource::class.java.classLoader
                .getResourceAsStream("webhook-admin-ui/index.html")
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: error("Missing webhook admin UI template")
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun page(): Response {
        auth.realm().requireManageRealm()

        val html = pageTemplate.replace("__REALM_NAME__", realm.name)
        return Response.ok(html, MediaType.TEXT_HTML_TYPE).build()
    }

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    fun getConfig(): Response {
        auth.realm().requireManageRealm()
        return Response.ok(readConfig()).build()
    }

    @PUT
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateConfig(config: WebhookUiConfig?): Response {
        auth.realm().requireManageRealm()

        val nextConfig = config ?: throw BadRequestException("Missing config payload")
        realm.setAttribute(CONFIG_ATTRIBUTE, gson.toJson(nextConfig))

        return Response.ok(nextConfig).build()
    }

    @GET
    @Path("meta")
    @Produces(MediaType.APPLICATION_JSON)
    fun meta(): Response {
        auth.realm().requireManageRealm()

        val meta = mapOf(
            "realm" to realm.name,
            "providers" to mapOf(
                "http" to true,
                "amqp" to true,
                "syslog" to true,
            ),
            "note" to "Demo UI stub backed by realm attributes"
        )

        return Response.ok(meta).build()
    }

    private fun readConfig(): WebhookUiConfig {
        val raw = realm.getAttribute(CONFIG_ATTRIBUTE)?.trim().orEmpty()
        if (raw.isEmpty()) {
            return WebhookUiConfig()
        }

        return runCatching { gson.fromJson(raw, WebhookUiConfig::class.java) }
            .getOrElse { WebhookUiConfig() }
    }
}
