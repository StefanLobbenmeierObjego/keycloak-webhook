package com.vymalo.keycloak.webhook.syslog

import com.vymalo.keycloak.webhook.core.AbstractWebhookEventListenerFactory
import com.vymalo.keycloak.webhook.core.helper.syslogAppName
import com.vymalo.keycloak.webhook.core.helper.syslogFacility
import com.vymalo.keycloak.webhook.core.helper.syslogHostname
import com.vymalo.keycloak.webhook.core.helper.syslogMessageFormat
import com.vymalo.keycloak.webhook.core.helper.syslogProtocol
import com.vymalo.keycloak.webhook.core.helper.syslogServerHostname
import com.vymalo.keycloak.webhook.core.helper.syslogServerPort
import com.vymalo.keycloak.webhook.core.helper.syslogSeverity
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.provider.ProviderConfigurationBuilder

open class SyslogWebhookFactory : AbstractWebhookEventListenerFactory(SyslogWebhookHandler()) {
    companion object {
        private val CONFIG_METADATA = ProviderConfigurationBuilder.create()
            .property()
            .name(syslogProtocol)
            .label("Protocol")
            .helpText("Syslog transport protocol.")
            .type(ProviderConfigProperty.LIST_TYPE)
            .defaultValue("UDP")
            .options("UDP", "TCP")
            .required(true)
            .add()
            .property()
            .name(syslogHostname)
            .label("Hostname")
            .helpText("Hostname to include in emitted syslog messages.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .required(true)
            .add()
            .property()
            .name(syslogAppName)
            .label("Application name")
            .helpText("Application name to include in syslog messages.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("Keycloak")
            .add()
            .property()
            .name(syslogFacility)
            .label("Facility")
            .helpText("Syslog facility value.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("USER")
            .add()
            .property()
            .name(syslogSeverity)
            .label("Severity")
            .helpText("Default syslog severity.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("INFORMATIONAL")
            .add()
            .property()
            .name(syslogServerHostname)
            .label("Server host")
            .helpText("Hostname of the syslog server.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .required(true)
            .add()
            .property()
            .name(syslogServerPort)
            .label("Server port")
            .helpText("Port of the syslog server.")
            .type(ProviderConfigProperty.INTEGER_TYPE)
            .defaultValue(5514)
            .required(true)
            .add()
            .property()
            .name(syslogMessageFormat)
            .label("Message format")
            .helpText("Syslog message format.")
            .type(ProviderConfigProperty.LIST_TYPE)
            .defaultValue("RFC_5425")
            .options("RFC_3164", "RFC_5424", "RFC_5425")
            .required(true)
            .add()
            .build()
    }

    override fun getAdditionalConfigMetadata(): List<ProviderConfigProperty> = CONFIG_METADATA
}
