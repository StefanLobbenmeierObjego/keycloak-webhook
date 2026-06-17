package com.vymalo.keycloak.webhook.core.admin

import org.keycloak.Config
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory

class WebhookAdminRealmResourceProviderFactory : AdminRealmResourceProviderFactory {
    override fun create(session: KeycloakSession): AdminRealmResourceProvider = WebhookAdminRealmResourceProvider()

    override fun init(config: Config.Scope) {}

    override fun postInit(factory: KeycloakSessionFactory) {}

    override fun close() {}

    override fun getId(): String = "webhook-ui"
}
