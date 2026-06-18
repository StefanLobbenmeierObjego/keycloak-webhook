package com.vymalo.keycloak.webhook.core

interface WebhookConfigProvider {
    fun getConfig(key: String): String?
}
