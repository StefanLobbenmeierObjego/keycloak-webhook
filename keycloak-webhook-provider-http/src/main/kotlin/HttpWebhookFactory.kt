package com.vymalo.keycloak.webhook.http

import com.vymalo.keycloak.webhook.core.AbstractWebhookEventListenerFactory
import com.vymalo.keycloak.webhook.core.helper.httpAuthPasswordKey
import com.vymalo.keycloak.webhook.core.helper.httpAuthUsernameKey
import com.vymalo.keycloak.webhook.core.helper.httpBaseBathKey
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.provider.ProviderConfigurationBuilder

open class HttpWebhookFactory : AbstractWebhookEventListenerFactory(HttpWebhookHandler()) {
    companion object {
        private val CONFIG_METADATA = ProviderConfigurationBuilder.create()
            .property()
            .name(httpBaseBathKey)
            .label("Base URLs")
            .helpText("Comma-separated webhook endpoint URLs.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .required(true)
            .add()
            .property()
            .name(httpAuthUsernameKey)
            .label("Basic auth username")
            .helpText("Optional username used for HTTP basic authentication.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name(httpAuthPasswordKey)
            .label("Basic auth password")
            .helpText("Optional password used for HTTP basic authentication.")
            .type(ProviderConfigProperty.PASSWORD)
            .secret(true)
            .add()
            .build()
    }

    override fun getAdditionalConfigMetadata(): List<ProviderConfigProperty> = CONFIG_METADATA
}
