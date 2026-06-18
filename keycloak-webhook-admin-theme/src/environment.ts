import { getInjectedEnvironment } from "@keycloak/keycloak-ui-shared";
import type { AdminEnvironment } from "@keycloak/keycloak-admin-ui";

export const environment = getInjectedEnvironment<AdminEnvironment>();
