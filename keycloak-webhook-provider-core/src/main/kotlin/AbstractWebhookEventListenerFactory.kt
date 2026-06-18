package com.vymalo.keycloak.webhook.core

import com.vymalo.keycloak.webhook.core.helper.cf
import com.vymalo.keycloak.webhook.core.helper.eventsTakenKey
import org.keycloak.Config
import org.keycloak.events.Event
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.events.admin.AdminEvent
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.provider.ProviderConfigurationBuilder
import org.keycloak.provider.ServerInfoAwareProviderFactory
import org.slf4j.LoggerFactory
import java.math.BigDecimal

abstract class AbstractWebhookEventListenerFactory(
    private val delegate: WebhookHandler
) : EventListenerProviderFactory,
    ServerInfoAwareProviderFactory,
    EventListenerProvider,
    WebhookHandler by delegate {
    private var takeList: Set<String>? = null

    override fun getOperationalInfo() = mapOf("version" to "0.10.0-rc.1")

    companion object {
        private val COMMON_CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
            .property()
            .name(eventsTakenKey)
            .label("Events filter")
            .helpText("Comma-separated list of user/admin event types to forward. Leave empty to forward all events.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .build()

        @JvmStatic
        private val LOG = LoggerFactory.getLogger(AbstractWebhookEventListenerFactory::class.java)
    }

    override fun getConfigMetadata(): List<ProviderConfigProperty> =
        COMMON_CONFIG_PROPERTIES + getAdditionalConfigMetadata()

    protected open fun getAdditionalConfigMetadata(): List<ProviderConfigProperty> = emptyList()

    override fun create(session: KeycloakSession): EventListenerProvider {
        ensureParametersInit(session)
        return this
    }

    @Synchronized
    private fun ensureParametersInit(session: KeycloakSession) {
        synchronized(delegate) {
            val configProvider = RealmComponentWebhookConfigProvider(session, delegate.getId())
            delegate.initHandler(configProvider)

            takeList = eventsTakenKey.cf(configProvider)
                ?.trim()
                ?.split(",")
                ?.map { it.trim() }
                ?.toSet()
        }
    }

    override fun init(config: Config.Scope) {}

    override fun postInit(factory: KeycloakSessionFactory) {}

    override fun onEvent(event: Event) = send(
        event.id,
        event.time,
        event.realmId,
        event.getEventRealmName(),
        event.sessionId,
        event.clientId,
        event.userId,
        event.ipAddress,
        event.type.toString(),
        event.error,
        event.details,
        null,
        null
    )

    override fun onEvent(event: AdminEvent, includeRepresentation: Boolean) = send(
        event.id,
        event.time,
        event.realmId,
        event.getAdminEventRealmName(),
        null,
        event.authDetails?.clientId,
        event.authDetails?.userId,
        event.authDetails?.ipAddress,
        "${event.resourceType}-${event.operationType}",
        event.error,
        null,
        event.resourcePath,
        event.representation
    )

    private fun send(
        id: String,
        time: Long?,
        realmId: String,
        realmName: String?,
        sessionId: String?,
        clientId: String?,
        userId: String?,
        ipAddress: String?,
        type: String,
        error: String?,
        details: Map<String, Any>?,
        resourcePath: String?,
        representation: String?,
    ) {
        if (takeList != null && type !in takeList!!) {
            LOG.debug("Event {} not in the taken list. Will be skipped ({}).", type, takeList)
            return
        }

        val request = WebhookPayload(
            id = id,
            time = if (time == null) null else BigDecimal(time),
            clientId = clientId,
            userId = userId,
            realmId = realmId,
            realmName = realmName,
            sessionId = sessionId,
            ipAddress = ipAddress,
            type = type,
            details = details,
            error = error,
            resourcePath = resourcePath,
            representation = representation
        )

        try {
            LOG.debug("Sending [{}] webhook for event type {}: {}", delegate.getId(), type, request)
            delegate.sendWebhook(request)
        } catch (e: Throwable) {
            LOG.error("Could not send webhook", e)
        }
    }
}
