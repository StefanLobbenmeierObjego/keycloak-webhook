import {
  type AppRouteObject,
  NotFoundRoute,
  routes as baseRoutes,
} from "@keycloak/keycloak-admin-ui";
import {
  RealmSettingsRoute,
  RealmSettingsRouteWithTab,
} from "./realm-settings-routes";

const replacedPaths = new Set([
  "/:realm/realm-settings",
  "/:realm/realm-settings/:tab",
]);

export type { AppRouteObject };

export const routes: AppRouteObject[] = [
  ...baseRoutes.filter((route) => !replacedPaths.has(route.path) && route.path !== "*"),
  RealmSettingsRoute,
  RealmSettingsRouteWithTab,
  NotFoundRoute,
];
