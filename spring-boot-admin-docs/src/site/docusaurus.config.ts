import 'dotenv/config';
import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const globalVariables = {
  VERSION: process.env.VERSION,
}

const config: Config = {
  title: 'Spring Boot Admin',
  favicon: 'img/favicon.png',
  url: 'https://docs.spring-boot-admin.com/',
  baseUrl: '/',
  organizationName: 'codecentric',
  projectName: 'spring-boot-admin',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  i18n: {
    defaultLocale: "en",
    locales: ["en"]
  },
  presets: [
    [
      "classic",
      {
        docs: {
          sidebarPath: "./sidebars.ts",
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ["rss", "atom"],
            xslt: true
          },
          onInlineTags: "warn",
          onInlineAuthors: "warn",
          onUntruncatedBlogPosts: "warn"
        },
      } satisfies Preset.Options
    ]
  ],
  markdown: {
    preprocessor: ({fileContent}) => {
      let content = fileContent;
      for (const variable in globalVariables) {
        content = content.replaceAll('@'+variable+'@', globalVariables[variable]);
      }

      return content
    },
  },
  themeConfig: {
    image: "img/social-card.jpg",
    tableOfContents: {
      minHeadingLevel: 2,
      maxHeadingLevel: 5
    },
    algolia: {
      appId: "GUDRYGX7B3",
      apiKey: "97d8206755fc8cf80c3b435ac87573ca",
      indexName: "spring-boot-admin",
      contextualSearch: true,
      replaceSearchResultPathname: {
        from: "/docs/",
        to: "/"
      },
      searchParameters: {},
      searchPagePath: false,
      insights: false
    },
    navbar: {
      title: "Spring Boot Admin",
      logo: {
        alt: "Spring Boot logo with pulse line in front of it",
        src: "img/logo.png"
      },
      items: [
        {
          type: "docSidebar",
          sidebarId: "tutorialSidebar",
          position: "left",
          label: "Documentation"
        },
        {
          type: "docSidebar",
          sidebarId: "tutorialSidebar",
          position: "left",
          label: "FAQ",
          href: "/faq"
        },
        {
          href: "https://github.com/codecentric/spring-boot-admin",
          label: "GitHub",
          position: "right"
        }
      ]
    },
    footer: {
      style: "dark",
      links: [
        {
          title: "Docs",
          items: [
            {
              label: "Getting started",
              to: "/docs/getting-started"
            },
            {
              label: "FAQ",
              to: "/faq"
            }
          ]
        },
        {
          title: "Community",
          items: [
            {
              label: "Stack Overflow",
              href: "https://stackoverflow.com/questions/tagged/spring-boot-admin"
            }
          ]
        },
        {
          title: "More",
          items: [
            {
              label: "GitHub",
              href: "https://github.com/codecentric/spring-boot-admin"
            },
            {
              label: "Impressum",
              to: "/impressum"
            },
            {
              label: "Privacy",
              to: "/privacy"
            }
          ]
        }
      ],
      copyright: `Copyright © ${new Date().getFullYear()} codecentric`
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ["java"]
    }
  } satisfies Preset.ThemeConfig
};

export default config;
