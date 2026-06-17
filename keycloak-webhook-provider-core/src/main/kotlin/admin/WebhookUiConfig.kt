package com.vymalo.keycloak.webhook.core.admin

data class WebhookUiConfig(
    val general: GeneralWebhookUiConfig = GeneralWebhookUiConfig(),
    val http: HttpWebhookUiConfig = HttpWebhookUiConfig(),
    val amqp: AmqpWebhookUiConfig = AmqpWebhookUiConfig(),
    val syslog: SyslogWebhookUiConfig = SyslogWebhookUiConfig(),
)

data class GeneralWebhookUiConfig(
    val eventsTaken: List<String> = emptyList(),
)

data class HttpWebhookUiConfig(
    val enabled: Boolean = true,
    val baseUrls: List<String> = listOf("http://prism:4010"),
    val username: String = "",
    val password: String = "",
)

data class AmqpWebhookUiConfig(
    val enabled: Boolean = false,
    val host: String = "rabbitmq",
    val port: Int = 5672,
    val vHost: String = "/",
    val exchange: String = "keycloak",
    val username: String = "",
    val password: String = "",
    val ssl: Boolean = false,
    val usePublisherConfirm: Boolean = false,
    val publisherConfirmTimeout: Long = 5000,
)

data class SyslogWebhookUiConfig(
    val enabled: Boolean = false,
    val protocol: String = "UDP",
    val hostname: String = "keycloak",
    val appName: String = "Keycloak",
    val facility: String = "USER",
    val severity: String = "INFORMATIONAL",
    val serverHostname: String = "syslog-ng",
    val serverPort: Int = 5514,
    val messageFormat: String = "RFC_5425",
)
