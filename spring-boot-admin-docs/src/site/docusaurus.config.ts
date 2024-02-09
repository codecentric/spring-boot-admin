import { themes as prismThemes } from "prism-react-renderer";
import type * as Preset from "@docusaurus/preset-classic";
import "dotenv/config";
import { readFileSync } from "fs";

export function readVersionFromPom() {
  const regex = /<revision>(?<revision>.*)<\/revision>/gm;
  const pom = readFileSync("../../../pom.xml", "utf8");
  return pom.matchAll(regex).next().value.groups.revision;
}

const version = readVersionFromPom();

export default async function createConfigAsync() {
  const title = `Spring Boot Admin`;

  const config = {
    title,
    tagline: "The Monitoring Tool for Spring Boot Applications",
    favicon: "img/favicon.ico",

    customFields: {
      version
    },

    url: "https://docs.spring-boot-admin.com",
    baseUrl: "/",

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: "codecentric", // Usually your GitHub org/user name.
    projectName: "spring-boot-admin", // Usually your repo name.

    onBrokenLinks: "throw",
    onBrokenMarkdownLinks: "warn",

    // Even if you don't use internationalization, you can use this field to set
    // useful metadata like html lang. For example, if your site is Chinese, you
    // may want to replace "en" with "zh-Hans".
    i18n: {
      defaultLocale: "en",
      locales: ["en"]
    },

    presets: [
      [
        "classic",
        {
          docs: {
            sidebarPath: "./sidebars.ts"
          },
          blog: {
            showReadingTime: true
          },
          theme: {
            customCss: "./src/css/custom.css"
          }
        } satisfies Preset.Options
      ]
    ],

    themeConfig: {
      algolia: {
        // The application ID provided by Algolia
        appId: process.env.ALGOLIA_APP_ID,

        // Public API key: it is safe to commit it
        apiKey: process.env.ALGOLIA_SEARCH_API_KEY,

        indexName: process.env.ALGOLIA_INDEX_NAME,

        // Optional: see doc section below
        contextualSearch: true,

        // Optional: Specify domains where the navigation should occur through window.location instead on history.push. Useful when our Algolia config crawls multiple documentation sites and we want to navigate with window.location.href to them.
        externalUrlRegex: "external\\.com|domain\\.com",

        // Optional: Replace parts of the item URLs from Algolia. Useful when using the same search index for multiple deployments using a different baseUrl. You can use regexp or string in the `from` param. For example: localhost:3000 vs myCompany.com/docs
        replaceSearchResultPathname: {
          from: "/docs/", // or as RegExp: /\/docs\//
          to: "/"
        },

        // Optional: Algolia search parameters
        searchParameters: {},

        // Optional: path for search page that enabled by default (`false` to disable it)
        searchPagePath: "search"

        //... other Algolia params
      },
      // Replace with your project's social card
      image: "img/docusaurus-social-card.jpg",
      navbar: {
        title,
        logo: {
          alt: "My Site Logo",
          src: "img/logo.svg"
        },
        items: [
          {
            type: 'html',
            position: 'left',
            value: `<em><small>${version}</small></em>`
          },
          {
            type: "docSidebar",
            sidebarId: "tutorialSidebar",
            position: "left",
            label: "Tutorial"
          },
          {
            href: "https://github.com/facebook/docusaurus",
            label: "GitHub",
            position: "right"
          }
        ]
      },
      footer: {
        style: "dark",
        links: [
          {
            // Left empty
          },
          {
            title: "Community",
            items: [
              {
                label: "Stack Overflow",
                href: "https://stackoverflow.com/questions/tagged/docusaurus"
              }
            ]
          },
          {
            title: "More",
            items: [
              {
                label: "GitHub",
                href: "https://github.com/codecentric/spring-boot-admin"
              }
            ]
          }
        ],
        copyright: `Copyright © ${new Date().getFullYear()} My Project, Inc. Built with Docusaurus.`
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula
      }
    } satisfies Preset.ThemeConfig
  };
  return config;
}
