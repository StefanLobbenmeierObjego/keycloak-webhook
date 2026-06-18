import { createInstance } from "i18next";
import FetchBackend from "i18next-fetch-backend";
import { initReactI18next } from "react-i18next";
import { environment } from "./environment";

const joinPath = (...paths: string[]) =>
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

type MessageItem = { key: string; value: string };

export const i18n = createInstance({
  fallbackLng: "en",
  keySeparator: ".",
  nsSeparator: false,
  interpolation: {
    escapeValue: false,
  },
  defaultNS: [environment.realm],
  ns: [environment.realm],
  backend: {
    loadPath: joinPath(
      environment.adminBaseUrl,
      `resources/{{ns}}/admin/{{lng}}`,
    ),
    parse: (data: string) => {
      const messages: MessageItem[] = JSON.parse(data);
      return Object.fromEntries(messages.map(({ key, value }) => [key, value]));
    },
  },
});

i18n.use(FetchBackend);
i18n.use(initReactI18next);
