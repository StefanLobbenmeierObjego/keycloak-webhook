package com.vymalo.keycloak.webhook.http.models

import com.vymalo.keycloak.webhook.core.DefaultWebhookConfigProvider
import com.vymalo.keycloak.webhook.core.WebhookConfigProvider
import com.vymalo.keycloak.webhook.core.helper.*

data class HttpConfig(
    val username: String?,
    val password: String?,
    val baseUrls: List<String>,
) {
    companion object {
        fun fromEnv(configProvider: WebhookConfigProvider = DefaultWebhookConfigProvider()): HttpConfig = HttpConfig(
            username = httpAuthUsernameKey.cf(configProvider),
            password = httpAuthPasswordKey.cf(configProvider),
            baseUrls = httpBaseBathKey.cff(configProvider).split(',')
        )
    }
}
