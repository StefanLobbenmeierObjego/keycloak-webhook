import type { TFunction } from "i18next";
import { isBundleKey, label } from "@keycloak/keycloak-ui-shared";

export const joinPath = (...paths: string[]) =>
  paths
    .map((path, index) => {
      let next = path;
      if (index > 0) {
        next = next.replace(/^\/+/, "");
      }
      if (index < paths.length - 1) {
        next = next.replace(/\/+$/, "");
      }
      return next;
    })
    .join("/");

export const getAuthorizationHeaders = (accessToken?: string) =>
  accessToken ? { Authorization: `Bearer ${accessToken}` } : {};

export const resolveDisplayName = (
  t: TFunction,
  displayName?: string,
  fallback = "",
) => {
  if (displayName && isBundleKey(displayName)) {
    return label(t, displayName);
  }

  return displayName || fallback;
};
