export type WebhookUiConfig = {
  general: {
    eventsTaken: string[];
  };
  http: {
    enabled: boolean;
    baseUrls: string[];
    username: string;
    password: string;
  };
  amqp: {
    enabled: boolean;
    host: string;
    port: number;
    vHost: string;
    exchange: string;
    username: string;
    password: string;
    ssl: boolean;
    usePublisherConfirm: boolean;
    publisherConfirmTimeout: number;
  };
  syslog: {
    enabled: boolean;
    protocol: string;
    hostname: string;
    appName: string;
    facility: string;
    severity: string;
    serverHostname: string;
    serverPort: number;
    messageFormat: string;
  };
};

export type WebhookMeta = {
  realm: string;
  providers: Record<string, boolean>;
  note?: string;
};

export const defaultWebhookConfig: WebhookUiConfig = {
  general: {
    eventsTaken: [],
  },
  http: {
    enabled: true,
    baseUrls: ["http://prism:4010"],
    username: "",
    password: "",
  },
  amqp: {
    enabled: false,
    host: "rabbitmq",
    port: 5672,
    vHost: "/",
    exchange: "keycloak",
    username: "",
    password: "",
    ssl: false,
    usePublisherConfirm: false,
    publisherConfirmTimeout: 5000,
  },
  syslog: {
    enabled: false,
    protocol: "UDP",
    hostname: "keycloak",
    appName: "Keycloak",
    facility: "USER",
    severity: "INFORMATIONAL",
    serverHostname: "syslog-ng",
    serverPort: 5514,
    messageFormat: "RFC_5425",
  },
};
