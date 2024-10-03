import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const config: Config = {
  title: "My Site",
  tagline: "Dinosaurs are cool",
  favicon: "img/favicon.ico",
  url: "https://your-docusaurus-site.example.com",
  baseUrl: "/",
  organizationName: "facebook", // Usually your GitHub org/user name.
  projectName: "docusaurus", // Usually your repo name.
  onBrokenLinks: "warn",
  onBrokenMarkdownLinks: "warn",
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
        theme: {
          customCss: "./src/css/custom.css"
        }
      } satisfies Preset.Options
    ]
  ],

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
