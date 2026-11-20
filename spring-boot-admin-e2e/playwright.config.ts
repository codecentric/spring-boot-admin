import { defineConfig, devices, Project } from '@playwright/test';
import { existsSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));

const sampleJar = resolve(
  here,
  process.env.SBA_SAMPLE_JAR ??
    '../spring-boot-admin-samples/spring-boot-admin-sample-servlet/target/spring-boot-admin-sample-servlet.jar',
);

if (!existsSync(sampleJar)) {
  throw new Error(
    `Sample jar not found at ${sampleJar}. Build it first, e.g. ` +
      '"./mvnw -B install -DskipTests -pl spring-boot-admin-samples/spring-boot-admin-sample-servlet -am"',
  );
}

/**
 * Each scenario starts the servlet sample (server + self-registering client) with a different
 * configuration. These combinations caused most of the "UI completely broken" regressions
 * (context path, Spring Security login/logout, custom UI extensions).
 */
export const scenarios = {
  insecure: {
    port: 18765,
    secure: false,
    contextPath: '',
    args: ['--spring.profiles.active=insecure'],
  },
  secure: {
    port: 18766,
    secure: true,
    contextPath: '/admin',
    args: ['--spring.profiles.active=secure', '--server.servlet.context-path=/admin'],
  },
} as const;

export type ScenarioName = keyof typeof scenarios;

const scenarioNames = Object.keys(scenarios) as ScenarioName[];

const baseUrl = (name: ScenarioName) =>
  `http://localhost:${scenarios[name].port}${scenarios[name].contextPath}`;

const authFile = (name: ScenarioName) => resolve(here, `target/.auth/${name}.json`);

const startCommand = (name: ScenarioName) => {
  const { port, args } = scenarios[name];
  const url = baseUrl(name);
  return [
    'java',
    '-jar',
    JSON.stringify(sampleJar),
    `--server.port=${port}`,
    `--spring.boot.admin.client.url=${url}`,
    `--spring.boot.admin.client.instance.service-base-url=http://localhost:${port}`,
    '--spring.boot.admin.client.instance.metadata.service-url=' + url,
    '--spring.boot.admin.discovery.enabled=false',
    '--spring.cloud.config.enabled=false',
    `--logging.file.name=target/logs/${name}.log`,
    ...args,
  ].join(' ');
};

/**
 * Secured scenarios get a setup project that signs in once and stores the session
 * (https://playwright.dev/docs/auth). All other tests of the scenario reuse it.
 */
const projectsFor = (name: ScenarioName): Project[] => {
  const { secure, contextPath } = scenarios[name];
  const shared = {
    use: { ...devices['Desktop Chrome'], baseURL: baseUrl(name) + '/' },
    metadata: { secure, contextPath, authFile: authFile(name) },
  };
  if (!secure) {
    return [{ name, ...shared }];
  }
  return [
    { name: `${name}-setup`, testMatch: /auth\.setup\.ts/, ...shared },
    {
      name,
      dependencies: [`${name}-setup`],
      ...shared,
      use: { ...shared.use, storageState: authFile(name) },
    },
  ];
};

export default defineConfig({
  testDir: './tests',
  outputDir: './target/test-results',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  timeout: 60_000,
  expect: { timeout: 15_000 },
  reporter: process.env.CI
    ? [['github'], ['html', { outputFolder: 'target/playwright-report', open: 'never' }]]
    : [['list'], ['html', { outputFolder: 'target/playwright-report', open: 'never' }]],
  use: {
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    locale: 'en-US',
    timezoneId: 'Europe/Berlin',
  },
  projects: scenarioNames.flatMap(projectsFor),
  webServer: scenarioNames.map((name) => ({
    command: startCommand(name),
    url: `${baseUrl(name)}/actuator/info`,
    reuseExistingServer: !process.env.CI,
    timeout: 180_000,
    stdout: 'ignore',
    stderr: 'pipe',
    gracefulShutdown: { signal: 'SIGTERM', timeout: 10_000 },
  })),
});
