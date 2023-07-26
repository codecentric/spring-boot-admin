export const SITE = {
  title: "Spring Boot Admin",
  description: "Your website description.",
  defaultLanguage: "en-us",
} as const;

export const OPEN_GRAPH = {
  image: {
    src: "https://github.com/codecentric/spring-boot-admin/blob/master/images/logo-spring-boot-admin.png?raw=true",
    alt:
      "astro logo on a starry expanse of space," +
      " with a purple saturn-like planet floating in the right foreground",
  },
  twitter: "astrodotbuild",
};

export const KNOWN_LANGUAGES = {
  English: "en",
} as const;
export const KNOWN_LANGUAGE_CODES = Object.values(KNOWN_LANGUAGES);

export const GITHUB_EDIT_URL = `https://github.com/codecentric/spring-boot-admin`;

export const ALGOLIA = {
  indexName: "dev_docs",
  appId: "G9FEY71VTH",
  apiKey: "43db92079ef0ad6f92fef7856d8054f6",
  enabled: true,
};

export type Sidebar = Record<
  (typeof KNOWN_LANGUAGE_CODES)[number],
  Record<string, { text: string; link: string }[]>
>;
export const SIDEBAR: Sidebar = {
  en: {
    "Start here": [{ text: "Getting Started", link: "en/getting-started" }],
    "SBA Server": [
      { text: "Client Discovery", link: "en/server/discovery" },
      { text: "Notifications", link: "en/server/notifiers" },
      { text: "Clustering", link: "en/server/clustering" },
      { text: "Security", link: "en/server/security" },
      { text: "Customizing", link: "en/server/customize_ui" },
    ],
    "SBA Client": [
      { text: "Configuration", link: "en/client" },
      { text: "Custom Interceptors", link: "en/client/actuator_interceptors" },
      { text: "Custom HTTP Headers", link: "en/client/http-headers" },
      { text: "Properties", link: "en/client/properties" },
    ],
    Development: [{ text: "Snapshot-Versions", link: "en/snapshot-versions" }],
    "": [
      { text: "Impressum", link: "en/impressum" },
      { text: "Datenschutzerklärung", link: "en/datenschutzerklaerung" },
    ],
  },
};
