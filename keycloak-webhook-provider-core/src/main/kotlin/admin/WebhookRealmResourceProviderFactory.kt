package com.vymalo.keycloak.webhook.core.admin

import org.keycloak.Config
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resource.RealmResourceProviderFactory

class WebhookRealmResourceProviderFactory : RealmResourceProviderFactory {
    override fun create(session: KeycloakSession): RealmResourceProvider = WebhookRealmResourceProvider(session)

    override fun init(config: Config.Scope) {}

    override fun postInit(factory: KeycloakSessionFactory) {}

    override fun close() {}

    override fun getId(): String = "webhook-ui"
}
