package com.vymalo.keycloak.webhook.amqp.models

import com.vymalo.keycloak.webhook.core.DefaultWebhookConfigProvider
import com.vymalo.keycloak.webhook.core.WebhookConfigProvider
import com.vymalo.keycloak.webhook.core.helper.*

data class AmqpConfig(
    val username: String,
    val password: String,
    val host: String,
    val port: String,
    val vHost: String?,
    val ssl: Boolean,
    val exchange: String,
    val usePublisherConfirm: Boolean,
    val publisherConfirmTimeout: String?
) {
    companion object {
        fun fromEnv(configProvider: WebhookConfigProvider = DefaultWebhookConfigProvider()): AmqpConfig = AmqpConfig(
            username = amqpUsernameKey.cff(configProvider),
            password = amqpPasswordKey.cff(configProvider),
            host = amqpHostKey.cff(configProvider),
            port = amqpPortKey.cff(configProvider),
            vHost = amqpVHostKey.cf(configProvider),
            ssl = amqpSsl.bf(configProvider = configProvider),
            exchange = amqpExchangeKey.cff(configProvider),
            usePublisherConfirm = amqpEnablePublisherConfirm.bf(configProvider = configProvider),
            publisherConfirmTimeout = amqpPublisherConfirmTimeout.cf(configProvider)
        )
    }
}
