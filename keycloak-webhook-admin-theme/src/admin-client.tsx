import type KeycloakAdminClient from "@keycloak/keycloak-admin-client";
import {
  AdminClientContext,
  initAdminClient,
  KeycloakSpinner,
  useEnvironment,
} from "@keycloak/keycloak-admin-ui";
import type Keycloak from "keycloak-js";
import { PropsWithChildren, useEffect, useState } from "react";
import type { Environment } from "./environment-types";

export type AdminClientProps = {
  keycloak: Keycloak;
  adminClient: KeycloakAdminClient;
};

export const AdminClientProvider = ({ children }: PropsWithChildren) => {
  const { keycloak, environment } = useEnvironment<Environment>();
  const [adminClient, setAdminClient] = useState<KeycloakAdminClient>();

  useEffect(() => {
    void initAdminClient(keycloak, environment).then(setAdminClient).catch(console.error);
  }, [environment, keycloak]);

  if (!adminClient) {
    return <KeycloakSpinner />;
  }

  return (
    <AdminClientContext.Provider value={{ keycloak, adminClient }}>
      {children}
    </AdminClientContext.Provider>
  );
};
