package com.vymalo.keycloak.webhook.core

import com.vymalo.keycloak.webhook.core.storage.WebhookUserStorageProviderFactory
import org.keycloak.models.KeycloakSession

class RealmComponentWebhookConfigProvider(
    private val session: KeycloakSession,
    private val providerId: String,
    private val fallback: WebhookConfigProvider = DefaultWebhookConfigProvider(),
) : WebhookConfigProvider {
    override fun getConfig(key: String): String? {
        val realm = session.context.realm ?: return fallback.getConfig(key)

        val component = realm.getComponentsStream(
            WebhookUserStorageProviderFactory.PROVIDER_TYPE,
            WebhookUserStorageProviderFactory.PROVIDER_ID,
        ).findFirst().orElse(null)

        val componentValue = component?.config?.get(key)?.firstOrNull()?.trim().orEmpty()
        if (componentValue.isNotEmpty()) {
            return componentValue
        }

        val providerSpecificValue = component?.config?.get("${providerId}.$key")?.firstOrNull()?.trim().orEmpty()
        if (providerSpecificValue.isNotEmpty()) {
            return providerSpecificValue
        }

        return fallback.getConfig(key)
    }
}
