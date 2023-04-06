export const SITE = {
  title: "Spring Boot Admin",
  description: "Your website description.",
  defaultLanguage: "en-us",
} as const;

export const OPEN_GRAPH = {
  image: {
    src: "https://github.com/withastro/astro/blob/main/.github/assets/banner.png?raw=true",
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

export const GITHUB_EDIT_URL = `https://github.com/withastro/astro/tree/main/examples/docs`;

// See "Algolia" section of the README for more information.
export const ALGOLIA = {
  indexName: "spring-boot-admin",
  appId: "G9FEY71VTH",
  apiKey: "43db92079ef0ad6f92fef7856d8054f6",
  enabled: false,
};

export type Sidebar = Record<
  (typeof KNOWN_LANGUAGE_CODES)[number],
  Record<string, { text: string; link: string }[]>
>;
export const SIDEBAR: Sidebar = {
  en: {
    "Start here": [
      { text: "Getting Started", link: "en/getting-started" },
      { text: "Use Snapshots", link: "en/snapshot-versions" },
    ],
    "SBA Server": [
      { text: "Discover", link: "en/server/discovery" },
      { text: "Notifiers", link: "en/server/notifiers" },
      { text: "Clustering", link: "en/server/clustering" },
      { text: "Security", link: "en/server/security" },
    ],
    "SBA Client": [
      { text: "Configuration", link: "en/client" },
      { text: "Customize UI", link: "en/client/customize_ui" },
      { text: "Custom Interceptors", link: "en/client/customize_interceptors" },
      { text: "Custom HTTP Headers", link: "en/client/customize_http-headers" },
    ],
  },
};
