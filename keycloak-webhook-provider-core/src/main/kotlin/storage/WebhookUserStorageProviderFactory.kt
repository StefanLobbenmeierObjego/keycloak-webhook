package com.vymalo.keycloak.webhook.core.storage

import com.vymalo.keycloak.webhook.core.helper.amqpEnablePublisherConfirm
import com.vymalo.keycloak.webhook.core.helper.amqpExchangeKey
import com.vymalo.keycloak.webhook.core.helper.amqpHostKey
import com.vymalo.keycloak.webhook.core.helper.amqpPasswordKey
import com.vymalo.keycloak.webhook.core.helper.amqpPortKey
import com.vymalo.keycloak.webhook.core.helper.amqpPublisherConfirmTimeout
import com.vymalo.keycloak.webhook.core.helper.amqpSsl
import com.vymalo.keycloak.webhook.core.helper.amqpUsernameKey
import com.vymalo.keycloak.webhook.core.helper.amqpVHostKey
import com.vymalo.keycloak.webhook.core.helper.eventsTakenKey
import com.vymalo.keycloak.webhook.core.helper.httpAuthPasswordKey
import com.vymalo.keycloak.webhook.core.helper.httpAuthUsernameKey
import com.vymalo.keycloak.webhook.core.helper.httpBaseBathKey
import com.vymalo.keycloak.webhook.core.helper.syslogAppName
import com.vymalo.keycloak.webhook.core.helper.syslogFacility
import com.vymalo.keycloak.webhook.core.helper.syslogHostname
import com.vymalo.keycloak.webhook.core.helper.syslogMessageFormat
import com.vymalo.keycloak.webhook.core.helper.syslogProtocol
import com.vymalo.keycloak.webhook.core.helper.syslogServerHostname
import com.vymalo.keycloak.webhook.core.helper.syslogServerPort
import com.vymalo.keycloak.webhook.core.helper.syslogSeverity
import org.keycloak.component.ComponentModel
import org.keycloak.component.ComponentValidationException
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.provider.ProviderConfigurationBuilder
import org.keycloak.storage.UserStorageProviderFactory

class WebhookUserStorageProviderFactory : UserStorageProviderFactory<WebhookUserStorageProvider> {
    companion object {
        const val PROVIDER_ID = "webhook-config"
        const val PROVIDER_TYPE = "org.keycloak.storage.UserStorageProvider"

        private val CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
            .property().name(eventsTakenKey).label("Events filter").helpText("Comma-separated list of user/admin event types to forward. Leave empty to forward all events.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(httpBaseBathKey).label("HTTP base URLs").helpText("Comma-separated webhook endpoint URLs.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(httpAuthUsernameKey).label("HTTP basic auth username").helpText("Optional username used for HTTP basic authentication.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(httpAuthPasswordKey).label("HTTP basic auth password").helpText("Optional password used for HTTP basic authentication.").type(ProviderConfigProperty.PASSWORD).secret(true).add()
            .property().name(amqpHostKey).label("AMQP host").helpText("AMQP broker hostname.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(amqpPortKey).label("AMQP port").helpText("AMQP broker port.").type(ProviderConfigProperty.INTEGER_TYPE).defaultValue(5672).add()
            .property().name(amqpVHostKey).label("AMQP virtual host").helpText("Optional AMQP virtual host.").type(ProviderConfigProperty.STRING_TYPE).defaultValue("/").add()
            .property().name(amqpExchangeKey).label("AMQP exchange").helpText("AMQP exchange to publish webhook events to.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(amqpUsernameKey).label("AMQP username").helpText("AMQP username.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(amqpPasswordKey).label("AMQP password").helpText("AMQP password.").type(ProviderConfigProperty.PASSWORD).secret(true).add()
            .property().name(amqpSsl).label("AMQP SSL").helpText("Enable TLS when connecting to the broker.").type(ProviderConfigProperty.BOOLEAN_TYPE).defaultValue(false).add()
            .property().name(amqpEnablePublisherConfirm).label("AMQP publisher confirms").helpText("Wait for broker publish confirmations after sending each message.").type(ProviderConfigProperty.BOOLEAN_TYPE).defaultValue(false).add()
            .property().name(amqpPublisherConfirmTimeout).label("AMQP confirm timeout (ms)").helpText("Timeout in milliseconds while waiting for publisher confirms.").type(ProviderConfigProperty.INTEGER_TYPE).defaultValue(5000).add()
            .property().name(syslogProtocol).label("Syslog protocol").helpText("Syslog transport protocol.").type(ProviderConfigProperty.LIST_TYPE).defaultValue("UDP").options("UDP", "TCP").add()
            .property().name(syslogHostname).label("Syslog hostname").helpText("Hostname to include in emitted syslog messages.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(syslogAppName).label("Syslog application name").helpText("Application name to include in syslog messages.").type(ProviderConfigProperty.STRING_TYPE).defaultValue("Keycloak").add()
            .property().name(syslogFacility).label("Syslog facility").helpText("Syslog facility value.").type(ProviderConfigProperty.STRING_TYPE).defaultValue("USER").add()
            .property().name(syslogSeverity).label("Syslog severity").helpText("Default syslog severity.").type(ProviderConfigProperty.STRING_TYPE).defaultValue("INFORMATIONAL").add()
            .property().name(syslogServerHostname).label("Syslog server host").helpText("Hostname of the syslog server.").type(ProviderConfigProperty.STRING_TYPE).add()
            .property().name(syslogServerPort).label("Syslog server port").helpText("Port of the syslog server.").type(ProviderConfigProperty.INTEGER_TYPE).defaultValue(5514).add()
            .property().name(syslogMessageFormat).label("Syslog message format").helpText("Syslog message format.").type(ProviderConfigProperty.LIST_TYPE).defaultValue("RFC_5425").options("RFC_3164", "RFC_5424", "RFC_5425").add()
            .build()
    }

    override fun create(session: KeycloakSession, model: ComponentModel): WebhookUserStorageProvider = WebhookUserStorageProvider()

    override fun getId(): String = PROVIDER_ID

    override fun getHelpText(): String = "Stores webhook transport settings for the webhook event listeners."

    override fun getConfigProperties(): List<ProviderConfigProperty> = CONFIG_PROPERTIES

    override fun validateConfiguration(session: KeycloakSession, realm: RealmModel, config: ComponentModel) {
        val duplicates = realm.getComponentsStream(PROVIDER_TYPE, PROVIDER_ID)
            .filter { it.id != config.id }
            .findAny()
            .isPresent

        if (duplicates) {
            throw ComponentValidationException("Only one webhook-config provider can be configured per realm")
        }
    }
}
