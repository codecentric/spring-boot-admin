import { defineConfig } from 'astro/config';
import preact from '@astrojs/preact';
import react from '@astrojs/react';

import tailwind from '@astrojs/tailwind';

let config = defineConfig({
  integrations: [
    tailwind({
      applyBaseStyles: false
    }),
    // Enable Preact to support Preact JSX components.
    preact(),
    // Enable React for the Algolia search component.
    react()],
  site: `http://localhost:3000/3.0.3-SNAPSHOT/`,
  base: `/3.0.3-SNAPSHOT/`,
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

// https://astro.build/config
export default defineConfig(config);
