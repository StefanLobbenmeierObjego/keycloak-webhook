import { fetchWithError } from "@keycloak/keycloak-admin-client";
import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import type { UserProfileConfig } from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import {
  EventsTab,
  KeysTab,
  LocalizationTab,
  PartialExportDialog,
  PartialImportDialog,
  PoliciesTab,
  RealmSettingsEmailTab,
  RealmSettingsGeneralTab,
  RealmSettingsLoginTab,
  RealmSettingsSessionsTab,
  RealmSettingsTokensTab,
  RoutableTabs,
  SecurityDefenses,
  ThemesTab,
  UserProfileTab,
  UserRegistration,
  ViewHeader,
  useAccess,
  useAdminClient,
  useAlerts,
  useConfirmDialog,
  useRealm,
} from "@keycloak/keycloak-admin-ui";
import {
  AlertVariant,
  ButtonVariant,
  Divider,
  DropdownItem,
  PageSection,
  Tab,
  TabTitleText,
} from "@patternfly/react-core";
import { useEnvironment } from "@keycloak/keycloak-ui-shared";
import { useEffect, useState } from "react";
import { Controller, FormProvider, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import type { Environment } from "./environment-types";
import helpUrls from "./help-urls";
import { useRoutableTab } from "./routable-tabs";
import useIsFeatureEnabled, { Feature } from "./useIsFeatureEnabled";
import { getAuthorizationHeaders, joinPath, resolveDisplayName } from "./utils";
import { WebhooksTab } from "./WebhooksTab";
import { type RealmSettingsTab, toRealmSettings } from "./realm-settings-routes";

type RealmLoAMappingType = {
  acr: string;
  loa: string;
  uri?: string;
};

export interface UIRealmRepresentation extends RealmRepresentation {
  upConfig?: UserProfileConfig;
}

const beerify = (name: string) => name.replaceAll(".", "🍺");
const debeerify = (name: string) => name.replaceAll("🍺", ".");

const isAttributesObject = (value: unknown) =>
  typeof value === "object" &&
  value !== null &&
  Object.values(value as Record<string, unknown>).some(
    (item) => Array.isArray(item) && item.length >= 1,
  );

const isAttributeArray = (value: unknown) =>
  Array.isArray(value) &&
  value.some(
    (entry) =>
      typeof entry === "object" &&
      entry !== null &&
      "key" in entry &&
      "value" in entry,
  );

const arrayToKeyValue = (record: Record<string, string[]>) =>
  Object.entries(record).map(([key, value]) => ({ key, value }));

const keyValueToArray = (entries: Array<{ key: string; value: string | string[] }>) =>
  Object.fromEntries(
    entries
      .filter(({ key }) => key)
      .map(({ key, value }) => [key, Array.isArray(value) ? value : [value]]),
  );

const flattenObject = (value: Record<string, unknown>, prefix = ""): Record<string, unknown> =>
  Object.entries(value).reduce<Record<string, unknown>>((acc, [key, next]) => {
    const resolved = prefix ? `${prefix}.${key}` : key;
    if (next && typeof next === "object" && !Array.isArray(next)) {
      Object.assign(acc, flattenObject(next as Record<string, unknown>, resolved));
    } else {
      acc[resolved] = next;
    }
    return acc;
  }, {});

const convertToFormValues = (
  obj: Record<string, unknown>,
  setValue: ReturnType<typeof useForm>["setValue"],
) => {
  Object.entries(obj).forEach(([key, value]) => {
    if (key === "attributes" && isAttributesObject(value)) {
      setValue(key, arrayToKeyValue(value as Record<string, string[]>));
      return;
    }

    if (key === "config" || key === "attributes") {
      const flattened = flattenObject((value as Record<string, unknown>) || {});
      Object.entries(flattened).forEach(([flattenedKey, flattenedValue]) => {
        setValue(`${key}.${beerify(flattenedKey)}`, flattenedValue);
      });
      return;
    }

    setValue(key, value);
  });
};

const convertFormValuesToObject = <T extends Record<string, unknown>>(obj: T) => {
  const result: Record<string, unknown> = {};
  Object.entries(obj).forEach(([key, value]) => {
    if (isAttributeArray(value)) {
      result[key] = keyValueToArray(value as Array<{ key: string; value: string | string[] }>);
      return;
    }

    if (key === "config" || key === "attributes") {
      result[key] = Object.fromEntries(
        Object.entries((value as Record<string, unknown>) || {}).map(([nestedKey, nestedValue]) => [
          debeerify(nestedKey),
          nestedValue,
        ]),
      );
      return;
    }

    result[key] = value;
  });
  return result;
};

type HeaderProps = {
  onChange: (value: boolean) => void;
  value: boolean;
  save: () => void;
  realmName: string;
  refresh: () => void;
};

const RealmSettingsHeader = ({ save, onChange, value, realmName, refresh }: HeaderProps) => {
  const { adminClient } = useAdminClient();
  const { environment } = useEnvironment<Environment>();
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();
  const navigate = useNavigate();
  const [partialImportOpen, setPartialImportOpen] = useState(false);
  const [partialExportOpen, setPartialExportOpen] = useState(false);
  const { hasAccess } = useAccess();
  const canManageRealm = hasAccess("manage-realm");

  const [toggleDisableDialog, DisableConfirm] = useConfirmDialog({
    titleKey: "disableConfirmTitle",
    messageKey: "disableConfirmRealm",
    continueButtonLabel: "disable",
    onConfirm: () => {
      onChange(!value);
      save();
    },
  });

  const [toggleDeleteDialog, DeleteConfirm] = useConfirmDialog({
    titleKey: "deleteConfirmTitle",
    messageKey: "deleteConfirmRealmSetting",
    continueButtonLabel: "delete",
    continueButtonVariant: ButtonVariant.danger,
    onConfirm: async () => {
      try {
        await adminClient.realms.del({ realm: realmName });
        addAlert(t("deletedSuccessRealmSetting"), AlertVariant.success);
        navigate({ pathname: `/${environment.masterRealm}` });
        refresh();
      } catch (error) {
        addError("deleteErrorRealmSetting", error);
      }
    },
  });

  return (
    <>
      <DisableConfirm />
      <DeleteConfirm />
      <PartialImportDialog
        open={partialImportOpen}
        toggleDialog={() => setPartialImportOpen(!partialImportOpen)}
      />
      <PartialExportDialog
        isOpen={partialExportOpen}
        onClose={() => setPartialExportOpen(false)}
      />
      <ViewHeader
        titleKey={realmName}
        noTranslate
        subKey="realmSettingsExplain"
        helpUrl={helpUrls.realmSettingsUrl}
        divider={false}
        dropdownItems={[
          <DropdownItem
            key="import"
            data-testid="openPartialImportModal"
            isDisabled={!canManageRealm}
            onClick={() => setPartialImportOpen(true)}
          >
            {t("partialImport")}
          </DropdownItem>,
          <DropdownItem
            key="export"
            data-testid="openPartialExportModal"
            isDisabled={!canManageRealm}
            onClick={() => setPartialExportOpen(true)}
          >
            {t("partialExport")}
          </DropdownItem>,
          <Divider key="separator" />,
          <DropdownItem key="delete" isDisabled={!canManageRealm} onClick={toggleDeleteDialog}>
            {t("delete")}
          </DropdownItem>,
        ]}
        isEnabled={value}
        isReadOnly={!canManageRealm}
        onToggle={(enabled) => {
          if (!enabled) {
            toggleDisableDialog();
          } else {
            onChange(enabled);
            save();
          }
        }}
      />
    </>
  );
};

export const RealmSettingsTabs = () => {
  const { adminClient } = useAdminClient();
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();
  const { realm: realmName, realmRepresentation: realm, refresh } = useRealm();
  const navigate = useNavigate();
  const isFeatureEnabled = useIsFeatureEnabled();
  const [tableData, setTableData] = useState<Record<string, string>[] | undefined>();
  const form = useForm({ mode: "onChange" });
  const { control, setValue, getValues } = form;
  const [localizationKey, setLocalizationKey] = useState(0);

  const refreshHeader = () => {
    setLocalizationKey((current) => current + 1);
  };

  useEffect(() => {
    convertToFormValues(realm as unknown as Record<string, unknown>, setValue);
  }, [realm, setValue]);

  useEffect(() => {
    const loadLocalizationTexts = async () => {
      const locales = realm.supportedLocales?.length
        ? Array.from(new Set([...(realm.defaultLocale ? [realm.defaultLocale] : []), ...realm.supportedLocales]))
        : [realm.defaultLocale || "en"];

      try {
        await Promise.all(
          locales.map(async (locale) => {
            try {
              const response = (await adminClient.realms.getRealmLocalizationTexts({
                realm: realmName,
                selectedLocale: locale,
              })) as Record<string, string> | undefined;

              if (response) {
                setTableData([response]);
              }
            } catch {
              return [];
            }
          }),
        );
      } catch {
        return [];
      }
    };

    void loadLocalizationTexts();
  }, [adminClient.realms, realm, realmName]);

  const save = async (nextRealm: UIRealmRepresentation) => {
    const realmToSave = convertFormValuesToObject(nextRealm) as UIRealmRepresentation;

    if (
      realmToSave.attributes?.["acr.loa.map"] &&
      typeof realmToSave.attributes["acr.loa.map"] !== "string"
    ) {
      const mappings = realmToSave.attributes["acr.loa.map"] as unknown as RealmLoAMappingType[];
      if (isFeatureEnabled(Feature.StepUpAuthenticationSaml)) {
        realmToSave.attributes["acr.uri.map"] = JSON.stringify(
          Object.fromEntries(
            mappings
              .filter(({ acr, uri }) => acr !== "" && uri && uri !== "")
              .map(({ acr, uri }) => [acr, uri]),
          ),
        );
      }

      realmToSave.attributes["acr.loa.map"] = JSON.stringify(
        Object.fromEntries(
          mappings.filter(({ acr }) => acr !== "").map(({ acr, loa }) => [acr, loa]),
        ),
      );
    }

    try {
      const savedRealm: UIRealmRepresentation = {
        ...realm,
        ...realmToSave,
        id: realmToSave.realm,
      };

      if (savedRealm.smtpServer?.port === "") {
        savedRealm.smtpServer = { ...savedRealm.smtpServer, port: null };
      }

      const response = await fetchWithError(
        joinPath(adminClient.baseUrl, `admin/realms/${realmName}/ui-ext`),
        {
          method: "PUT",
          body: JSON.stringify(savedRealm),
          headers: {
            "Content-Type": "application/json",
            ...getAuthorizationHeaders(await adminClient.getAccessToken()),
          },
        },
      );

      if (!response.ok) {
        throw new Error(response.statusText);
      }

      addAlert(t("realmSaveSuccess"), AlertVariant.success);
    } catch (error) {
      addError("realmSaveError", error);
    }

    if (realmName !== (realmToSave.realm || realm.realm)) {
      navigate(toRealmSettings({ realm: realmToSave.realm!, tab: "general" }));
    }

    refresh();
  };

  const useTab = (tab: RealmSettingsTab) => useRoutableTab(toRealmSettings({ realm: realmName, tab }));

  const generalTab = useTab("general");
  const loginTab = useTab("login");
  const emailTab = useTab("email");
  const themesTab = useTab("themes");
  const keysTab = useTab("keys");
  const eventsTab = useTab("events");
  const localizationTab = useTab("localization");
  const securityDefensesTab = useTab("security-defenses");
  const sessionsTab = useTab("sessions");
  const tokensTab = useTab("tokens");
  const clientPoliciesTab = useTab("client-policies");
  const userProfileTab = useTab("user-profile");
  const userRegistrationTab = useTab("user-registration");
  const webhooksTab = useTab("webhooks");

  const { hasAccess, hasSomeAccess } = useAccess();
  const canViewOrManageEvents =
    hasAccess("view-realm") && hasSomeAccess("view-events", "manage-events");
  const canViewUserRegistration =
    hasAccess("view-realm") && hasSomeAccess("view-clients", "manage-clients");

  return (
    <FormProvider {...form}>
      <Controller
        name="enabled"
        defaultValue={true}
        control={control}
        render={({ field }) => (
          <RealmSettingsHeader
            value={field.value}
            onChange={field.onChange}
            realmName={resolveDisplayName(t, realm.displayName, realmName)}
            refresh={refreshHeader}
            save={() => void save(getValues() as UIRealmRepresentation)}
          />
        )}
      />
      <PageSection variant="light" className="pf-v5-u-p-0">
        <RoutableTabs
          isBox
          mountOnEnter
          aria-label="realm-settings-tabs"
          defaultLocation={toRealmSettings({ realm: realmName, tab: "general" })}
        >
          <Tab title={<TabTitleText>{t("general")}</TabTitleText>} data-testid="rs-general-tab" {...generalTab}>
            <RealmSettingsGeneralTab realm={realm} save={(value) => void save(value as UIRealmRepresentation)} />
          </Tab>
          <Tab title={<TabTitleText>{t("login")}</TabTitleText>} data-testid="rs-login-tab" {...loginTab}>
            <RealmSettingsLoginTab refresh={refresh} realm={realm} />
          </Tab>
          <Tab title={<TabTitleText>{t("email")}</TabTitleText>} data-testid="rs-email-tab" {...emailTab}>
            <RealmSettingsEmailTab realm={realm} save={(value) => void save(value as UIRealmRepresentation)} />
          </Tab>
          <Tab title={<TabTitleText>{t("themes")}</TabTitleText>} data-testid="rs-themes-tab" {...themesTab}>
            <ThemesTab realm={realm} save={(value) => void save(value as UIRealmRepresentation)} />
          </Tab>
          <Tab title={<TabTitleText>{t("keys")}</TabTitleText>} data-testid="rs-keys-tab" {...keysTab}>
            <KeysTab />
          </Tab>
          {canViewOrManageEvents && (
            <Tab title={<TabTitleText>{t("events")}</TabTitleText>} data-testid="rs-realm-events-tab" {...eventsTab}>
              <EventsTab realm={realm} />
            </Tab>
          )}
          <Tab title={<TabTitleText>{t("localization")}</TabTitleText>} data-testid="rs-localization-tab" {...localizationTab}>
            <LocalizationTab key={localizationKey} save={(value) => void save(value as UIRealmRepresentation)} realm={realm} tableData={tableData} />
          </Tab>
          <Tab title={<TabTitleText>{t("securityDefences")}</TabTitleText>} data-testid="rs-security-defenses-tab" {...securityDefensesTab}>
            <SecurityDefenses realm={realm} save={(value) => void save(value as UIRealmRepresentation)} />
          </Tab>
          <Tab title={<TabTitleText>{t("sessions")}</TabTitleText>} data-testid="rs-sessions-tab" {...sessionsTab}>
            <RealmSettingsSessionsTab key={localizationKey} realm={realm} save={(value) => void save(value as UIRealmRepresentation)} />
          </Tab>
          <Tab title={<TabTitleText>{t("tokens")}</TabTitleText>} data-testid="rs-tokens-tab" {...tokensTab}>
            <RealmSettingsTokensTab save={(value) => void save(value as UIRealmRepresentation)} realm={realm} />
          </Tab>
          {isFeatureEnabled(Feature.ClientPolicies) && (
            <Tab title={<TabTitleText>{t("clientPolicies")}</TabTitleText>} data-testid="rs-clientPolicies-tab" {...clientPoliciesTab}>
              <PoliciesTab />
            </Tab>
          )}
          <Tab title={<TabTitleText>{t("userProfile")}</TabTitleText>} data-testid="rs-user-profile-tab" {...userProfileTab}>
            <UserProfileTab setTableData={setTableData as never} />
          </Tab>
          {canViewUserRegistration && (
            <Tab title={<TabTitleText>{t("userRegistration")}</TabTitleText>} data-testid="rs-userRegistration-tab" {...userRegistrationTab}>
              <UserRegistration />
            </Tab>
          )}
          <Tab title={<TabTitleText>Webhooks</TabTitleText>} data-testid="rs-webhooks-tab" {...webhooksTab}>
            <WebhooksTab />
          </Tab>
        </RoutableTabs>
      </PageSection>
    </FormProvider>
  );
};
