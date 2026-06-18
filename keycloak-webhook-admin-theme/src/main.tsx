import "@patternfly/patternfly/patternfly-addons.css";
import "@patternfly/react-core/dist/styles/base.css";
import "@keycloak/keycloak-admin-ui/styles.css";

import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createHashRouter, RouterProvider } from "react-router-dom";
import { i18n } from "./i18n";
import { Root } from "./Root";
import { routes } from "./routes";

await i18n.init();

const router = createHashRouter([
  {
    path: "/",
    element: <Root />,
    children: routes,
  },
]);

const container = document.getElementById("app");
if (!container) {
  throw new Error("Missing app container");
}

createRoot(container).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
