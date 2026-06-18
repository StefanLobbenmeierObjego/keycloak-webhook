package com.vymalo.keycloak.webhook.core.admin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.ForbiddenException
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotAuthorizedException
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.services.managers.AppAuthManager
import org.keycloak.services.managers.AuthenticationManager
import org.keycloak.services.resources.admin.AdminAuth
import org.keycloak.services.resources.admin.fgap.AdminPermissions

class WebhookAdminResource(
    private val session: KeycloakSession,
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
        requireManageRealm()

        val html = pageTemplate.replace("__REALM_NAME__", realm.name)
        return Response.ok(html, MediaType.TEXT_HTML_TYPE).build()
    }

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    fun getConfig(): Response {
        requireManageRealm()
        return Response.ok(gson.toJson(readConfig()), MediaType.APPLICATION_JSON_TYPE).build()
    }

    @PUT
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateConfig(body: String?): Response {
        requireManageRealm()

        val nextConfig = body
            ?.takeIf { it.isNotBlank() }
            ?.let {
                runCatching { gson.fromJson(it, WebhookUiConfig::class.java) }
                    .getOrElse { throw BadRequestException("Invalid config payload") }
            }
            ?: throw BadRequestException("Missing config payload")

        realm.setAttribute(CONFIG_ATTRIBUTE, gson.toJson(nextConfig))

        return Response.ok(gson.toJson(nextConfig), MediaType.APPLICATION_JSON_TYPE).build()
    }

    @GET
    @Path("meta")
    @Produces(MediaType.APPLICATION_JSON)
    fun meta(): Response {
        requireManageRealm()

        val meta = mapOf(
            "realm" to realm.name,
            "providers" to mapOf(
                "http" to true,
                "amqp" to true,
                "syslog" to true,
            ),
            "note" to "Demo UI stub backed by realm attributes"
        )

        return Response.ok(gson.toJson(meta), MediaType.APPLICATION_JSON_TYPE).build()
    }

    private fun readConfig(): WebhookUiConfig {
        val raw = realm.getAttribute(CONFIG_ATTRIBUTE)?.trim().orEmpty()
        if (raw.isEmpty()) {
            return WebhookUiConfig()
        }

        return runCatching { gson.fromJson(raw, WebhookUiConfig::class.java) }
            .getOrElse { WebhookUiConfig() }
    }

    private val realm: RealmModel
        get() = session.context.realm ?: throw ForbiddenException("Realm context missing")

    private fun requireManageRealm() {
        val adminAuth = authenticateAdmin() ?: throw NotAuthorizedException("Bearer")
        AdminPermissions.evaluator(session, realm, adminAuth).realm().requireManageRealm()
    }

    private fun authenticateAdmin(): AdminAuth? {
        val authResult = authenticateBearerToken() ?: authenticateIdentityCookie() ?: return null
        return AdminAuth(realm, authResult.token, authResult.user, authResult.client)
    }

    private fun authenticateBearerToken(): AuthenticationManager.AuthResult? =
        AppAuthManager.BearerTokenAuthenticator(session)
            .setRealm(realm)
            .authenticate()

    private fun authenticateIdentityCookie(): AuthenticationManager.AuthResult? {
        return AuthenticationManager.authenticateIdentityCookie(session, realm, true)
    }
}
