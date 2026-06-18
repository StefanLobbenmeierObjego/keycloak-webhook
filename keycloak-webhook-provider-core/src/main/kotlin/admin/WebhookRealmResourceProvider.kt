package com.vymalo.keycloak.webhook.core.admin

import org.keycloak.models.KeycloakSession
import org.keycloak.services.resource.RealmResourceProvider

class WebhookRealmResourceProvider(
    private val session: KeycloakSession,
) : RealmResourceProvider {
    override fun getResource(): Any = WebhookAdminResource(session)

    override fun close() {}
}
