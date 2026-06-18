package com.vymalo.keycloak.webhook.syslog.models

import com.cloudbees.syslog.Facility
import com.cloudbees.syslog.MessageFormat
import com.cloudbees.syslog.Severity
import com.vymalo.keycloak.webhook.core.DefaultWebhookConfigProvider
import com.vymalo.keycloak.webhook.core.WebhookConfigProvider
import com.vymalo.keycloak.webhook.core.helper.*

data class SyslogConfig(
    val protocol: String,
    val hostname: String,
    val appName: String,
    val facility: Facility,
    val severity: Severity,
    val serverHostname: String,
    val serverPort: String,
    val messageFormat: MessageFormat,
) {
    companion object {
        fun fromEnv(configProvider: WebhookConfigProvider = DefaultWebhookConfigProvider()): SyslogConfig = SyslogConfig(
            protocol = syslogProtocol.cff(configProvider).uppercase(),
            hostname = syslogHostname.cff(configProvider),
            appName = syslogAppName.cff(configProvider),
            facility = Facility.valueOf(syslogFacility.cfe({ Facility.SYSLOG.name }, configProvider)),
            severity = Severity.valueOf(syslogSeverity.cfe({ Severity.INFORMATIONAL.name }, configProvider)),
            serverHostname = syslogServerHostname.cff(configProvider),
            serverPort = syslogServerPort.cff(configProvider),
            messageFormat = MessageFormat.valueOf(syslogMessageFormat.cfe({ MessageFormat.RFC_5425.name }, configProvider)),
        )
    }
}
