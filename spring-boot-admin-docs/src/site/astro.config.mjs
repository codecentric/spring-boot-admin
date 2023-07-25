import { defineConfig } from 'astro/config';
import preact from '@astrojs/preact';
import react from '@astrojs/react';

import tailwind from '@astrojs/tailwind';
import {readFileSync} from "fs";

function readVersionFromPom() {
  const regex = /<revision>(?<revision>.*)<\/revision>/gm;
  const pom = readFileSync("../../../pom.xml", "utf8");
  return pom.matchAll(regex).next().value.groups.revision;
}

const version = readVersionFromPom();

const config = defineConfig({
  integrations: [
    tailwind({
      applyBaseStyles: false
    }),
    // Enable Preact to support Preact JSX components.
    preact(),
    // Enable React for the Algolia search component.
    react()],
  site: `http://localhost:3000/${version}`,
  base: `/${version}`,
  markdown: {
    syntaxHighlight: 'prism'
  },
  outDir: '../../target/generated-docs'
});

if (process.env.CI) {
  config = {
    ...config,
    site: `https://docs.spring-boot-admin.com/${process.env.VERSION}/`,
    base: `/${process.env.VERSION}/`
  };
}

export default defineConfig(config);
