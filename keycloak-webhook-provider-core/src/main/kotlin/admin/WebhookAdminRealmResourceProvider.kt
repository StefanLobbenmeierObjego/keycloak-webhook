package com.vymalo.keycloak.webhook.core.admin

import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.services.resources.admin.AdminEventBuilder
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator

class WebhookAdminRealmResourceProvider : AdminRealmResourceProvider {
    override fun getResource(
        session: KeycloakSession,
        realm: RealmModel,
        auth: AdminPermissionEvaluator,
        adminEvent: AdminEventBuilder,
    ): Any = WebhookAdminResource(session, realm, auth)

    override fun close() {}
}
