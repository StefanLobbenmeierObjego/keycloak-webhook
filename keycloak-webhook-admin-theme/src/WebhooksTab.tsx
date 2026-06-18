import {
  Alert,
  Button,
  Form,
  FormGroup,
  Gallery,
  GalleryItem,
  PageSection,
  Spinner,
  Switch,
  TextArea,
  TextInput,
  Title,
} from "@patternfly/react-core";
import { useAdminClient, useRealm } from "@keycloak/keycloak-admin-ui";
import { useEnvironment } from "@keycloak/keycloak-ui-shared";
import { useEffect, useMemo, useState } from "react";
import type { Environment } from "./environment-types";
import { getAuthorizationHeaders, joinPath } from "./utils";
import {
  defaultWebhookConfig,
  type WebhookMeta,
  type WebhookUiConfig,
} from "./webhook-config";

type Status = {
  variant: "success" | "danger" | "info";
  message: string;
};

const splitLines = (value: string) =>
  value
    .split(/\r?\n|,/)
    .map((item) => item.trim())
    .filter(Boolean);

const joinLines = (value: string[]) => value.join("\n");

export const WebhooksTab = () => {
  const { adminClient } = useAdminClient();
  const { realm } = useRealm();
  const { environment } = useEnvironment<Environment>();
  const [config, setConfig] = useState<WebhookUiConfig>(defaultWebhookConfig);
  const [meta, setMeta] = useState<WebhookMeta>();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [status, setStatus] = useState<Status>();

  const baseUrl = useMemo(
    () => joinPath(environment.serverBaseUrl, `realms/${realm}/webhook-ui`),
    [environment.serverBaseUrl, realm],
  );

  const headers = async (contentType?: string) => ({
    ...(contentType ? { "Content-Type": contentType } : {}),
    Accept: "application/json",
    ...getAuthorizationHeaders(await adminClient.getAccessToken()),
  });

  const load = async () => {
    setLoading(true);
    setStatus(undefined);

    try {
      const [configResponse, metaResponse] = await Promise.all([
        fetch(`${baseUrl}/config`, {
          headers: await headers(),
        }),
        fetch(`${baseUrl}/meta`, {
          headers: await headers(),
        }),
      ]);

      if (!configResponse.ok) {
        throw new Error(`Failed to load config: ${configResponse.status}`);
      }

      if (!metaResponse.ok) {
        throw new Error(`Failed to load meta: ${metaResponse.status}`);
      }

      setConfig(await configResponse.json());
      setMeta(await metaResponse.json());
    } catch (error) {
      setStatus({
        variant: "danger",
        message: error instanceof Error ? error.message : "Failed to load webhooks.",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [baseUrl]);

  const save = async () => {
    setSaving(true);
    setStatus(undefined);

    try {
      const response = await fetch(`${baseUrl}/config`, {
        method: "PUT",
        headers: await headers("application/json"),
        body: JSON.stringify(config),
      });

      if (!response.ok) {
        throw new Error(`Failed to save config: ${response.status}`);
      }

      setConfig(await response.json());
      setStatus({ variant: "success", message: "Webhook configuration saved." });
    } catch (error) {
      setStatus({
        variant: "danger",
        message: error instanceof Error ? error.message : "Failed to save webhooks.",
      });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <PageSection>
        <Spinner size="xl" />
      </PageSection>
    );
  }

  return (
    <PageSection>
      <Form>
        <Title headingLevel="h2">Webhooks</Title>
        {meta && (
          <Alert
            isInline
            variant="info"
            title={`Realm ${meta.realm}`}
          >
            {Object.entries(meta.providers).map(([provider, enabled]) => (
              <div key={provider}>{provider}: {enabled ? "installed" : "missing"}</div>
            ))}
          </Alert>
        )}
        {status && (
          <Alert isInline variant={status.variant} title={status.message} />
        )}
        <Gallery hasGutter minWidths={{ default: "320px" }}>
          <GalleryItem>
            <FormGroup label="Events taken" fieldId="eventsTaken">
              <TextArea
                id="eventsTaken"
                value={joinLines(config.general.eventsTaken)}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    general: { eventsTaken: splitLines(value) },
                  })
                }
              />
            </FormGroup>
          </GalleryItem>
          <GalleryItem>
            <Title headingLevel="h3">HTTP</Title>
            <FormGroup label="Enabled" fieldId="httpEnabled">
              <Switch
                id="httpEnabled"
                isChecked={config.http.enabled}
                onChange={(_event, checked) =>
                  setConfig({
                    ...config,
                    http: { ...config.http, enabled: checked },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Base URLs" fieldId="httpBaseUrls">
              <TextArea
                id="httpBaseUrls"
                value={joinLines(config.http.baseUrls)}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    http: { ...config.http, baseUrls: splitLines(value) },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Username" fieldId="httpUsername">
              <TextInput
                id="httpUsername"
                value={config.http.username}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    http: { ...config.http, username: value },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Password" fieldId="httpPassword">
              <TextInput
                id="httpPassword"
                type="password"
                value={config.http.password}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    http: { ...config.http, password: value },
                  })
                }
              />
            </FormGroup>
          </GalleryItem>
          <GalleryItem>
            <Title headingLevel="h3">AMQP</Title>
            <FormGroup label="Enabled" fieldId="amqpEnabled">
              <Switch
                id="amqpEnabled"
                isChecked={config.amqp.enabled}
                onChange={(_event, checked) =>
                  setConfig({
                    ...config,
                    amqp: { ...config.amqp, enabled: checked },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Host" fieldId="amqpHost">
              <TextInput
                id="amqpHost"
                value={config.amqp.host}
                onChange={(_event, value) =>
                  setConfig({ ...config, amqp: { ...config.amqp, host: value } })
                }
              />
            </FormGroup>
            <FormGroup label="Port" fieldId="amqpPort">
              <TextInput
                id="amqpPort"
                type="number"
                value={String(config.amqp.port)}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    amqp: { ...config.amqp, port: Number(value || 0) },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Virtual host" fieldId="amqpVHost">
              <TextInput
                id="amqpVHost"
                value={config.amqp.vHost}
                onChange={(_event, value) =>
                  setConfig({ ...config, amqp: { ...config.amqp, vHost: value } })
                }
              />
            </FormGroup>
            <FormGroup label="Exchange" fieldId="amqpExchange">
              <TextInput
                id="amqpExchange"
                value={config.amqp.exchange}
                onChange={(_event, value) =>
                  setConfig({ ...config, amqp: { ...config.amqp, exchange: value } })
                }
              />
            </FormGroup>
          </GalleryItem>
          <GalleryItem>
            <Title headingLevel="h3">Syslog</Title>
            <FormGroup label="Enabled" fieldId="syslogEnabled">
              <Switch
                id="syslogEnabled"
                isChecked={config.syslog.enabled}
                onChange={(_event, checked) =>
                  setConfig({
                    ...config,
                    syslog: { ...config.syslog, enabled: checked },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Protocol" fieldId="syslogProtocol">
              <TextInput
                id="syslogProtocol"
                value={config.syslog.protocol}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    syslog: { ...config.syslog, protocol: value },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Hostname" fieldId="syslogHostname">
              <TextInput
                id="syslogHostname"
                value={config.syslog.hostname}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    syslog: { ...config.syslog, hostname: value },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Server hostname" fieldId="syslogServerHostname">
              <TextInput
                id="syslogServerHostname"
                value={config.syslog.serverHostname}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    syslog: { ...config.syslog, serverHostname: value },
                  })
                }
              />
            </FormGroup>
            <FormGroup label="Server port" fieldId="syslogServerPort">
              <TextInput
                id="syslogServerPort"
                type="number"
                value={String(config.syslog.serverPort)}
                onChange={(_event, value) =>
                  setConfig({
                    ...config,
                    syslog: { ...config.syslog, serverPort: Number(value || 0) },
                  })
                }
              />
            </FormGroup>
          </GalleryItem>
        </Gallery>
        <Button onClick={() => void save()} isLoading={saving}>
          Save
        </Button>
        <Button variant="secondary" onClick={() => void load()} isDisabled={saving}>
          Reload
        </Button>
      </Form>
    </PageSection>
  );
};
