import {
  AppContexts,
  ForbiddenSection,
  Header,
  PageBreadCrumbs,
  PageNav,
  useAccess,
} from "@keycloak/keycloak-admin-ui";
import {
  ErrorBoundaryFallback,
  KeycloakSpinner,
  mainPageContentId,
} from "@keycloak/keycloak-ui-shared";
import { Flex, FlexItem, Page } from "@patternfly/react-core";
import { Suspense, useEffect } from "react";
import { Outlet, useMatches } from "react-router-dom";
import { AdminClientProvider } from "./admin-client";
import { ErrorRenderer } from "./ErrorRenderer";

const AuthWall = ({ children }: { children: React.ReactNode }) => {
  const matches = useMatches();
  const { hasAccess } = useAccess();

  const permissionNeeded = matches.flatMap(({ handle }) => {
    if (!handle || typeof handle !== "object" || !("access" in handle)) {
      return [];
    }

    const access = (handle as { access: unknown }).access;
    return Array.isArray(access) ? access : [access];
  });

  if (!hasAccess(...permissionNeeded)) {
    return <ForbiddenSection permissionNeeded={permissionNeeded} />;
  }

  return <>{children}</>;
};

export const App = () => {
  const hrefEndsWithHashSlash = location.href.endsWith("#/");

  useEffect(() => {
    if (!hrefEndsWithHashSlash) {
      return;
    }

    history.replaceState(null, "", location.pathname);
  }, [hrefEndsWithHashSlash]);

  return (
    <AdminClientProvider>
      <AppContexts>
        <Flex
          direction={{ default: "column" }}
          flexWrap={{ default: "nowrap" }}
          spaceItems={{ default: "spaceItemsNone" }}
          style={{ height: "100%" }}
        >
          <FlexItem grow={{ default: "grow" }} style={{ minHeight: 0 }}>
            <Page
              header={<Header />}
              isManagedSidebar
              sidebar={<PageNav />}
              breadcrumb={<PageBreadCrumbs />}
              mainContainerId={mainPageContentId}
            >
              <ErrorBoundaryFallback fallback={ErrorRenderer}>
                <Suspense fallback={<KeycloakSpinner />}>
                  <AuthWall>
                    <Outlet />
                  </AuthWall>
                </Suspense>
              </ErrorBoundaryFallback>
            </Page>
          </FlexItem>
        </Flex>
      </AppContexts>
    </AdminClientProvider>
  );
};
