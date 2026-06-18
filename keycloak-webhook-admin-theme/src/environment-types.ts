import type { BaseEnvironment } from "@keycloak/keycloak-ui-shared";

export type Environment = BaseEnvironment & {
  adminBaseUrl: string;
  consoleBaseUrl: string;
  masterRealm: string;
  resourceVersion: string;
};
