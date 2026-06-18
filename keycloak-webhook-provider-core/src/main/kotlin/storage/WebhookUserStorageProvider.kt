package com.vymalo.keycloak.webhook.core.storage

import org.keycloak.storage.UserStorageProvider

class WebhookUserStorageProvider : UserStorageProvider {
    override fun close() {}
}
