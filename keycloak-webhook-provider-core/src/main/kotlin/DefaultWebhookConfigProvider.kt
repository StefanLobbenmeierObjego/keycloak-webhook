package com.vymalo.keycloak.webhook.core

class DefaultWebhookConfigProvider : WebhookConfigProvider {
    override fun getConfig(key: String): String? = System.getenv(key) ?: System.getProperty(key)
}
