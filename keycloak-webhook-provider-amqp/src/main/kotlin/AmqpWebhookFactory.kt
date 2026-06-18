package com.vymalo.keycloak.webhook.amqp

import com.vymalo.keycloak.webhook.core.AbstractWebhookEventListenerFactory
import com.vymalo.keycloak.webhook.core.helper.amqpEnablePublisherConfirm
import com.vymalo.keycloak.webhook.core.helper.amqpExchangeKey
import com.vymalo.keycloak.webhook.core.helper.amqpHostKey
import com.vymalo.keycloak.webhook.core.helper.amqpPasswordKey
import com.vymalo.keycloak.webhook.core.helper.amqpPortKey
import com.vymalo.keycloak.webhook.core.helper.amqpPublisherConfirmTimeout
import com.vymalo.keycloak.webhook.core.helper.amqpSsl
import com.vymalo.keycloak.webhook.core.helper.amqpUsernameKey
import com.vymalo.keycloak.webhook.core.helper.amqpVHostKey
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.provider.ProviderConfigurationBuilder

open class AmqpWebhookFactory : AbstractWebhookEventListenerFactory(AmqpWebhookHandler()) {
    companion object {
        private val CONFIG_METADATA = ProviderConfigurationBuilder.create()
            .property()
            .name(amqpHostKey)
            .label("Host")
            .helpText("AMQP broker hostname.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .required(true)
            .add()
            .property()
            .name(amqpPortKey)
            .label("Port")
            .helpText("AMQP broker port.")
            .type(ProviderConfigProperty.INTEGER_TYPE)
            .defaultValue(5672)
            .required(true)
            .add()
            .property()
            .name(amqpVHostKey)
            .label("Virtual host")
            .helpText("Optional AMQP virtual host.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("/")
            .add()
            .property()
            .name(amqpExchangeKey)
            .label("Exchange")
            .helpText("AMQP exchange to publish webhook events to.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .required(true)
            .add()
            .property()
            .name(amqpUsernameKey)
            .label("Username")
            .helpText("AMQP username.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .required(true)
            .add()
            .property()
            .name(amqpPasswordKey)
            .label("Password")
            .helpText("AMQP password.")
            .type(ProviderConfigProperty.PASSWORD)
            .secret(true)
            .required(true)
            .add()
            .property()
            .name(amqpSsl)
            .label("Use SSL")
            .helpText("Enable TLS when connecting to the broker.")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue(false)
            .add()
            .property()
            .name(amqpEnablePublisherConfirm)
            .label("Publisher confirms")
            .helpText("Wait for broker publish confirmations after sending each message.")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue(false)
            .add()
            .property()
            .name(amqpPublisherConfirmTimeout)
            .label("Confirm timeout (ms)")
            .helpText("Timeout in milliseconds while waiting for publisher confirms.")
            .type(ProviderConfigProperty.INTEGER_TYPE)
            .defaultValue(5000)
            .add()
            .build()
    }

    override fun getAdditionalConfigMetadata(): List<ProviderConfigProperty> = CONFIG_METADATA
}
