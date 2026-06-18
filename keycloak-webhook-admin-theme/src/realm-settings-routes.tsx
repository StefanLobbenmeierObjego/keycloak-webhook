import { lazy } from "react";
import type { Path } from "react-router-dom";
import type { AppRouteObject } from "./routes";

export type RealmSettingsTab =
  | "general"
  | "login"
  | "email"
  | "themes"
  | "keys"
  | "events"
  | "localization"
  | "security-defenses"
  | "sessions"
  | "tokens"
  | "client-policies"
  | "user-profile"
  | "user-registration"
  | "webhooks";

export type RealmSettingsParams = {
  realm: string;
  tab?: RealmSettingsTab;
};

const RealmSettingsSection = lazy(() => import("./RealmSettingsSection"));

export const RealmSettingsRoute: AppRouteObject = {
  path: "/:realm/realm-settings",
  element: <RealmSettingsSection />,
  handle: {
    access: "view-realm",
    breadcrumb: (t) => t("realmSettings"),
  },
};

export const RealmSettingsRouteWithTab: AppRouteObject = {
  ...RealmSettingsRoute,
  path: "/:realm/realm-settings/:tab",
};

const generateEncodedPath = (path: string, params: Record<string, string>) => {
  let result = path;
  for (const [key, value] of Object.entries(params)) {
    result = result.replace(`:${key}`, encodeURIComponent(value));
  }
  return result;
};

export const toRealmSettings = (params: RealmSettingsParams): Partial<Path> => {
  const path = params.tab ? RealmSettingsRouteWithTab.path : RealmSettingsRoute.path;
  return {
    pathname: generateEncodedPath(path, params as Record<string, string>),
  };
};
