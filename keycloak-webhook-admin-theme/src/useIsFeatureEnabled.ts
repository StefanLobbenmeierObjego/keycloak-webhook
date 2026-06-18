import { useAccess, useServerInfo } from "@keycloak/keycloak-admin-ui";

export enum Feature {
  ClientPolicies = "CLIENT_POLICIES",
  Organizations = "ORGANIZATION",
  Workflows = "WORKFLOWS",
  StepUpAuthenticationSaml = "STEP_UP_AUTHENTICATION_SAML",
}

export default function useIsFeatureEnabled() {
  const { features } = useServerInfo();
  const { hasAccess } = useAccess();

  const hasFeatureAccess = (feature: Feature) => {
    switch (feature) {
      case Feature.Organizations:
        return hasAccess(({ hasAny }) =>
          hasAny("manage-realm", "query-organizations"),
        );
      default:
        return true;
    }
  };

  return function isFeatureEnabled(feature: Feature) {
    if (!features) {
      return false;
    }

    return features
      .filter((item) => item.enabled && hasFeatureAccess(item.name as Feature))
      .map((item) => item.name)
      .includes(feature);
  };
}
